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

    init {
        if (!isPreviewMode) {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            fetchCurrentUserData()
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