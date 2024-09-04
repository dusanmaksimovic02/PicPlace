package com.example.picplace.models.place

import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.ui.screens.map.addplace.PollObjects
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

open class PlaceViewModel : ViewModel() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _topPlaces = MutableLiveData<List<PlaceFirebase>>(emptyList())
    val topPlaces: LiveData<List<PlaceFirebase>> = _topPlaces

    init {
        if (!isPreviewMode) {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        }
    }

    open fun addPlace(
        place: Place,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        try {
            val user = auth?.currentUser
            if (user == null) {
                onFailure("User not authenticated")
                return
            }

            if (place.imageUris.isEmpty()) {
                addPlaceToFirebase(place, user.uid, onSuccess, onFailure)
                return
            }

            val imageUrls = mutableListOf<String>()
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("places")
            place.imageUris.forEachIndexed { index, uri ->
                val imageRef =
                    storageRef.child("${user.uid}/${System.currentTimeMillis()}_$index.jpg")
                val uploadTask = imageRef.putFile(uri)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            onFailure(it.message.toString())
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageUrls.add(task.result.toString())
                        if (imageUrls.size == place.imageUris.size) {
                            val placeFirebase =
                                place.copy(imageUris = imageUrls.map { Uri.parse(it) })
                            addPlaceToFirebase(placeFirebase, user.uid, onSuccess, onFailure)
                        }
                    } else {
                        onFailure("Failed to upload image: ${task.exception?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Error in addPlace", e.message.toString())
        }
    }

    private fun addPlaceToFirebase(
        place: Place,
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        try {
            val placeFirebase = PlaceFirebase(
                name = place.name,
                description = place.description,
                imageUrls = place.imageUris.map { it.toString() },
                latLng = place.latLng,
                poll = place.poll,
                userId = userId,
                createdAt = place.createdAt
            )

            firestore?.collection("places")
                ?.add(placeFirebase)
                ?.addOnSuccessListener { documentReference ->
                    val generatedId = documentReference.id

                    val updatedPlace = placeFirebase.copy(id = generatedId)

                    firestore!!.collection("places")
                        .document(generatedId)
                        .set(updatedPlace)
                        .addOnSuccessListener {
                            updateUserScore(userId, 25)
                            onSuccess("Place added successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Error while adding place", e.toString())
                            onFailure("Failed to add place")
                        }
                }
                ?.addOnFailureListener { e ->
                    onFailure("Failed to add place: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("Error in addPlaceToFirebase", e.message.toString())
        }
    }

    private fun fetchTop5Places() {
        try {
            firestore?.collection("places")
                ?.orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(5)
                ?.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("FetchPlace", "Error while fetching users")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val placesList = snapshot.documents.mapNotNull { document ->
                            document.toObject(PlaceFirebase::class.java)
                        }
                        _topPlaces.value = placesList
                    }
                }
        } catch (e: Exception) {
            Log.e("Error in fetchTop5PLaces", e.message.toString())
        }
    }

    fun updateTopPlaces() {
        fetchTop5Places()
    }

    open suspend fun getPlaces(
        onSuccess: (List<PlaceFirebase>) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        try {
            firestore?.collection("places")
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    val places = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceFirebase::class.java)
                    }
                    onSuccess(places)
                }
                ?.addOnFailureListener { exception ->
                    onFailure("Failed to fetch places: ${exception.message}")
                }
                ?.await()
        } catch (e: Exception) {
            Log.e("Error in getPlaces", e.message.toString())
        }
    }

    suspend fun getFilteredPlaces(
        username: String? = null,
        name: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        radius: Double? = null,
        currentLocation: LatLng? = null,
        onSuccess: (List<PlaceFirebase?>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            var filteredPlaces: List<PlaceFirebase?> = emptyList()

            getPlaces(
                onSuccess = { places ->
                    filteredPlaces = places
                },
                onFailure = {}
            )

            if (username != null) {
                filteredPlaces = filteredPlaces.filter { it?.userName == username }
            }

            if (name != null) {
                filteredPlaces =
                    filteredPlaces.filter { it?.name!!.contains(name, ignoreCase = true) }
            }

            if (startDate != null && endDate != null) {
                filteredPlaces = filteredPlaces.filter {
                    it!!.createdAt in startDate..endDate
                }
            }

            if (radius != null && currentLocation != null) {
                filteredPlaces = filteredPlaces.filter { place ->
                    val distance = calculateDistance(currentLocation, place!!.latLng.toLatLng())
                    distance <= radius
                }
            }

            onSuccess(filteredPlaces)
        } catch (e: Exception) {
            Log.e("ERROR in getFilteredPlaces", e.toString())
            onFailure("Error while getting filtered places")
        }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun getPlacesForSpecificUser(
        id: String,
        onSuccess: (List<PlaceFirebase>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            firestore?.collection("places")
                ?.whereEqualTo("userId", id)
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    val places = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceFirebase::class.java)
                    }
                    onSuccess(places)
                }
                ?.addOnFailureListener { exception ->
                    onFailure("Failed to fetch places: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("Error in getPlaces", e.message.toString())
        }
    }

    fun updatePlace() {

    }

    fun deletePlace(
        placeId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val placeRef = FirebaseFirestore.getInstance().collection("places").document(placeId)

            placeRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val imagesList = document.get("imageUrls") as List<String>

                    val storage = FirebaseStorage.getInstance()
                    val deleteTasks = mutableListOf<Task<Void>>()

                    for (imageUrl in imagesList) {
                        val imageRef = storage.getReferenceFromUrl(imageUrl)
                        val deleteTask = imageRef.delete()
                        deleteTasks.add(deleteTask)
                    }

                    Tasks.whenAll(deleteTasks).addOnSuccessListener {
                        placeRef.delete().addOnSuccessListener {
                            Log.d("DeletePlace", "Place successfully deleted!")
                            onSuccess("Place successfully deleted!")
                        }.addOnFailureListener { e ->
                            Log.e("DeletePlace", "Error deleting place", e)
                            onFailure("Error deleting place")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("DeletePlace", "Error deleting images", e)
                    }
                } else {
                    Log.e("DeletePlace", "Place does not exist")
                }
            }.addOnFailureListener { e ->
                Log.e("DeletePlace", "Error fetching place", e)
            }
        } catch (e: Exception) {
            Log.e("Error in deletePlace", e.message.toString())
            onFailure("Error while deleting place")
        }
    }


    suspend fun submitPoll(
        placeId: String,
        userId: String,
        pollResults: SnapshotStateList<Int?>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        place: PlaceFirebase?
    ) {
        try {
            val result = mutableMapOf<String, Int>()

            pollResults.forEachIndexed { index, selectedIndex ->
                result[place!!.poll[index].question] = selectedIndex!!
            }

            val pollResult = PollResult(userId, result)

            firestore?.collection("places")
                ?.document(placeId)
                ?.update("pollResults", FieldValue.arrayUnion(pollResult))
                ?.addOnSuccessListener {
                    onSuccess("Poll submitted successfully")
                }
                ?.addOnFailureListener { e ->
                    onFailure("Error while submitting poll")
                    Log.e("Submitting poll", e.toString())
                }
                ?.await()
        } catch (e: Exception) {
            Log.e("Error in submitPoll", e.message.toString())
        }
    }

    suspend fun updatePollStatistics(
        placeId: String
    ) {
        try {
            getPlaceById(placeId) { place ->
                if (place == null) {
                    Log.e("POLL STATISTICS", "Place not found")
                    return@getPlaceById
                }

                val statistics = place.poll.map { pollObject ->
                    PollStatistics(
                        question = pollObject.question,
                        votesCount = pollObject.options.associateWith { 0 }.toMutableMap()
                    )
                }

                place.pollResults.forEach { pollResult ->
                    pollResult.votes.forEach { (question, selectedIndex) ->
                        val stat = statistics.find { it.question == question }
                        val option =
                            place.poll.find { it.question == question }?.options?.getOrNull(
                                selectedIndex
                            )
                        if (stat != null && option != null) {
                            stat.votesCount[option] = (stat.votesCount[option] ?: 0) + 1
                        }
                    }
                }

                firestore?.collection("places")
                    ?.document(placeId)
                    ?.update("pollStatistics", statistics)
                    ?.addOnSuccessListener {
                        Log.i("POLL STATISTICS", "Poll statistics updated successfully")
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("POLL STATISTICS", "Error updating poll statistics: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e("Error in updatePollStatistics", e.message.toString())
        }
    }

    suspend fun isUserTakePoll(
        placeId: String,
        userId: String
    ) : Boolean{
        try {
            val placeSnapshot = firestore?.collection("places")
                ?.document(placeId)
                ?.get()
                ?.await()

            if (placeSnapshot != null && placeSnapshot.exists()) {
                val pollResults = placeSnapshot.get("pollResults") as? List<Map<String, Int>>

                pollResults?.forEach { result ->
                    val resultUserId = result["userId"].toString()
                    if (resultUserId == userId) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Error in isUserTakePoll", e.message.toString())
        }
        return false
    }

    suspend fun likePlace(
        placeId: String,
        userId: String,
    ) {
        try {
            firestore?.collection("places")?.document(placeId)
                ?.update(
                    "likedBy", FieldValue.arrayUnion(userId),
                    "likes", FieldValue.increment(1)
                )
                ?.addOnSuccessListener {
                    updateUserScore(userId, 2)
                }
                ?.await()
        } catch (e: Exception) {
            Log.e("Error in likePLace", e.message.toString())
        }
    }

    suspend fun unlikePlace(
        placeId: String,
        userId: String,
    ) {
        try {
            firestore?.collection("places")?.document(placeId)
                ?.update(
                    "likedBy", FieldValue.arrayRemove(userId),
                    "likes", FieldValue.increment(-1)
                )
                ?.addOnSuccessListener {
                    updateUserScore(userId, -2)
                }
                ?.await()
        } catch (e: Exception) {
            Log.e("Error in unlikePlace", e.message.toString())
        }
    }

    suspend fun addComment(
        placeId: String,
        userId: String,
        userName: String,
        comment: String,
        profilePictureUrl: String,
    ) {
        try {
            getPlaceById(placeId) { place ->
                if (place != null) {
                    val newComment = Comment(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        userName = userName,
                        content = comment,
                        timestamp = System.currentTimeMillis(),
                        profilePictureUrl = profilePictureUrl
                    )

                    val updatedComments = place.comments.toMutableList().apply {
                        add(newComment)
                    }

                    firestore?.collection("places")
                        ?.document(placeId)
                        ?.update("comments", updatedComments)
                        ?.addOnSuccessListener {
                            Log.i("Adding comment", "Comment added successfully")
                            updateUserScore(userId, 5)
                        }
                        ?.addOnFailureListener {
                            Log.i("Adding comment", "Error while adding comment")
                        }
                } else {
                    Log.i("Adding comment", "Place with that id doesn't existing")
                }
            }
        } catch (e: Exception) {
            Log.e("Error in addComment", e.message.toString())
        }
    }

    suspend fun removeComment(
        placeId: String,
        commentId: String,
        userId: String
    ) {
        try {
            getPlaceById(placeId) { place ->
                if (place != null) {
                    val updatedComments = place.comments.filterNot { it.id == commentId }

                    firestore?.collection("places")
                        ?.document(placeId)
                        ?.update("comments", updatedComments)
                        ?.addOnSuccessListener {
                            Log.i("Remove comment", "Comment removed successfully")
                            updateUserScore(userId, -5)
                        }
                        ?.addOnFailureListener {
                            Log.i("Remove comment", "Error while removing comment")
                        }
                } else {
                    Log.i("Remove comment", "Place with that id doesn't existing")
                }
            }
        } catch (e: Exception) {
            Log.e("Error in removeComment", e.message.toString())
        }
    }

    suspend fun getPlaceById(
        placeId: String,
        onComplete: (PlaceFirebase?) -> Unit,
    ) {
        try {
            firestore?.collection("places")
                ?.whereEqualTo("id", placeId)
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    val place =
                        querySnapshot.documents.firstOrNull()?.toObject(PlaceFirebase::class.java)
                    onComplete(place)
                }
                ?.addOnFailureListener {
                    onComplete(null)
                }
                ?.await()
        } catch (e: Exception) {
            Log.e("Error in getPlaceById", e.message.toString())
        }
    }

    private fun updateUserScore(userId: String, pointsToAdd: Number) {
        try {
            firestore?.collection("users")
                ?.document(userId)
                ?.update("score", FieldValue.increment(pointsToAdd.toLong()))
                ?.addOnSuccessListener {
                    Log.i("Update Score", "User score updated by $pointsToAdd points")
                }
                ?.addOnFailureListener { exception ->
                    Log.e("Update Score", "Failed to update user score: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("Error in updateUserScore", e.message.toString())
        }
    }
}

data class PlaceFirebase(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val latLng: SerializableLatLng = SerializableLatLng(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(), // List of user IDs who liked the place
    val comments: List<Comment> = emptyList(),
    val poll: List<PollObjects> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val pollResults: List<PollResult> = emptyList(),
    val pollStatistics: List<PollStatistics> = emptyList(),
    val userName: String = ""
)

data class Place(
    val name: String = "",
    val description: String = "",
    val imageUris: List<Uri> = emptyList(),
    val poll: List<PollObjects> = emptyList(),
    val latLng: SerializableLatLng = SerializableLatLng(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val profilePictureUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

data class SerializableLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}

data class PollResult(
    val userId: String = "",
    val votes: Map<String, Int> = emptyMap()
)

data class PollStatistics(
    val question: String = "",
    val votesCount: MutableMap<String, Int> = mutableMapOf()
)