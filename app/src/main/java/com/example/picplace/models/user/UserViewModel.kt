package com.example.picplace.models.user

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.models.auth.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

open class UserViewModel() : ViewModel() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _userData = MutableLiveData<UserData?>()
    open val userData: LiveData<UserData?> get() = _userData

    private val _users = MutableLiveData<List<UserData>>(emptyList())
    val users: LiveData<List<UserData>> = _users

    private val _topUsers = MutableLiveData<List<UserData>>(emptyList())
    val topUsers: LiveData<List<UserData>> = _topUsers

    init {
        if (!isPreviewMode) {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            fetchCurrentUserData()
            fetchUsers()
            fetchTop5Users()
        }
    }

    private fun fetchCurrentUserData() {
        try {
            val userId = auth?.currentUser?.uid ?: return
            firestore?.collection("users")?.document(userId)?.get()
                ?.addOnSuccessListener { document ->
                    _userData.value = document?.toObject(UserData::class.java)
                }
                ?.addOnFailureListener {
                    _userData.value = null
                }
        } catch (e: Exception) {
            Log.e("Error in fetchUserData", e.message.toString())
        }
    }

    fun updateCurrentUser() {
        fetchCurrentUserData()
    }

    open suspend fun fetchUser(id: String) : UserData? {
        var user: UserData? = UserData()
        try {
            val document = firestore?.collection("users")?.document(id)?.get()?.await()
            user = document?.toObject(UserData::class.java)
            return user
        } catch (e: Exception) {
            Log.e("Error in fetchUserData", e.message.toString())
        }
        return UserData()
    }

    private fun fetchUsers() {
        try {
            firestore?.collection("users")
                ?.orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("FetchUsers", "Error while fetching users")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val userList = snapshot.documents.mapNotNull { document ->
                            document.toObject(UserData::class.java)
                        }
                        _users.value = userList
                    }
                }
        } catch (e: Exception) {
            Log.e("Error in fetchUsers", e.message.toString())
        }
    }

    private fun fetchTop5Users() {
        try {
            firestore?.collection("users")
                ?.orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(5)
                ?.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("FetchUsers", "Error while fetching users")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val userList = snapshot.documents.mapNotNull { document ->
                            document.toObject(UserData::class.java)
                        }
                        _topUsers.value = userList
                    }
                }
        } catch (e: Exception) {
            Log.e("Error in fetchTop5Users", e.message.toString())
        }
    }

    fun updateTopUsers() {
        fetchTop5Users()
    }

    fun updateUsers() {
        fetchUsers()
    }

    fun clearUserData() {
        _userData.value = UserData()
    }

    fun onLogin() {
        fetchCurrentUserData()
    }

    suspend fun changeUserData(
        name: String,
        surname: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val userId = auth?.currentUser?.uid ?: return
            val updates = mapOf(
                "name" to name,
                "surname" to surname,
                "phoneNumber" to phoneNumber
            )

            firestore?.collection("users")?.document(userId)
                ?.update(updates)
                ?.addOnSuccessListener {
                    onSuccess()
                    fetchCurrentUserData()
                }
                ?.addOnFailureListener { exception ->
                    onFailure(exception)
                }
                ?.await()
        } catch (e: Exception) {
            onFailure(e)
            Log.e("Error in changeUserData", e.message.toString())
        }
    }

    suspend fun changeUserEmail(
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val user = auth?.currentUser ?: return
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener() {
                    firestore?.collection("users")?.document(user.uid)
                        ?.update("email", newEmail)
                    onSuccess()
                    fetchCurrentUserData()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
                .await()
        } catch (e: Exception) {
            onFailure(e)
            Log.e("Error in changeUserEmail", e.message.toString())
        }
    }

    suspend fun changeUserUsername(
        newUsername: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val userId = auth?.currentUser?.uid ?: return
            firestore?.collection("users")?.document(userId)
                ?.update("username", newUsername)
                ?.addOnSuccessListener {
                    onSuccess()
                    fetchCurrentUserData()
                }
                ?.addOnFailureListener { exception ->
                    onFailure(exception)
                }
                ?.await()
        } catch (e: Exception) {
            onFailure(e)
            Log.e("Error in changeUserUsername", e.message.toString())
        }
    }

    suspend fun deleteAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val user = auth?.currentUser ?: return

            Firebase.storage.reference.child("profile_pictures/${user.uid}.jpg")
                .delete().addOnSuccessListener {
                    firestore?.collection("users")?.document(user.uid)?.delete()
                        ?.addOnSuccessListener {
                            user.delete()
                                .addOnSuccessListener {
                                    _userData.value = UserData()
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                        }
                        ?.addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }.addOnFailureListener { exception ->
                onFailure(exception)
                }
                .await()
        } catch (e: Exception) {
            onFailure(e)
            Log.e("Error in deleteAccount", e.message.toString())
        }
    }

    suspend fun changeProfilePicture(
        newPictureUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val user = auth?.currentUser ?: return
            val storageRef = Firebase.storage.reference.child("profile_pictures/${user.uid}.jpg")

            storageRef.putFile(newPictureUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        firestore?.collection("users")?.document(user.uid)
                            ?.update("imageUrl", uri.toString())
                            ?.addOnSuccessListener {
                                onSuccess()
                                fetchCurrentUserData()
                            }
                            ?.addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }.addOnFailureListener { exception ->
                        onFailure(exception)
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
                .await()
        } catch (e: Exception) {
            onFailure(e)
            Log.e("Error in changeProfilePicture", e.message.toString())
        }
    }
}