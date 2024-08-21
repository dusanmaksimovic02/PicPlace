package com.example.picplace.models.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

open class AuthViewModel : ViewModel() {
    private var auth: FirebaseAuth? = null
    private val _authState = MutableLiveData<AuthState>()
    open val authState: LiveData<AuthState> = _authState

    init {
        if (!isPreviewMode) {
            auth = FirebaseAuth.getInstance()
            checkAuthState()
        }
    }

    private fun checkAuthState() {
        if(auth?.currentUser == null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    open suspend fun login(username: String, password: String) {
        if(username.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email/Username or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        val email: String = if (isEmail(username)) {
            username
        } else {
            val emailFromDatabase = getEmailForUsername(username)
            emailFromDatabase
        }

        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Invalid username or email")
        } else {
            loginWithEmailAndPassword(
                email = email,
                password = password
            )
        }

    }

    open fun register(username: String, password: String, email: String, name: String, surname: String, phoneNumber: String, photoUri: Uri, onSuccess: () -> Unit, onFailure : () -> Unit) {
        _authState.value = AuthState.Loading
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener{task->
                if (task.isSuccessful){
                    auth?.currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                uploadProfilePicture(photoUri, onSuccess = { imageUrl ->
                                    saveUserData(
                                        username = username,
                                        email = email,
                                        name = name,
                                        surname = surname,
                                        phoneNumber = phoneNumber,
                                        imageUrl = imageUrl,
                                        onSuccess = {
                                            signOut()
                                            onSuccess()
                                        },
                                        onFailure = {
                                            onFailure()
                                        }
                                    )
                                }, onFailure = {
                                    _authState.value = AuthState.Error("Failed to upload profile picture")
                                    onFailure()
                                })
                            } else {
                                _authState.value = AuthState.Error(verificationTask.exception?.message ?: "Failed to send verification email")
                            }
                        }
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    private fun saveUserData(username: String, email: String, name: String, surname: String, phoneNumber: String, imageUrl: String, onSuccess: () -> Unit, onFailure : () -> Unit) {
        val currentUser = auth?.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
            onFailure()
            return
        }

        val user = UserData(
            email,
            username,
            name,
            surname,
            phoneNumber,
            imageUrl
        )

        Firebase.firestore.collection("users")
            .document(auth?.currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                _authState.value = AuthState.Authenticated
                onSuccess()
            }
            .addOnFailureListener {
                auth?.currentUser!!.delete()
                _authState.value = AuthState.Unauthenticated
                onFailure()
            }
    }

    private fun uploadProfilePicture(photoUri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val currentUser = auth?.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
            onFailure()
            return
        }

        val storageRef = Firebase.storage.reference.child("profile_pictures/${currentUser.uid}.jpg")

        storageRef.putFile(photoUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener {
                    _authState.value = AuthState.Error("Failed to get download URL")
                    onFailure()
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Failed to upload profile picture")
                onFailure()
            }
    }


    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email,password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth?.currentUser
                    if (currentUser?.isEmailVerified == true) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        signOut()
                        _authState.value = AuthState.Error("Please verify your email before logging in.")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    private fun isEmail(username: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    private suspend fun getEmailForUsername(username: String): String {
        val query = Firebase.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        val email = query.documents.firstOrNull()
        email?.getString("email") ?: ""

        return email?.getString("email").toString()
    }

    fun signOut() {
        auth?.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun forgotPassword(identifier: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (identifier.isEmpty()) {
            _authState.value = AuthState.Error("Identifier cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        when {
            isEmail(identifier) -> sendPasswordResetEmail(identifier, onSuccess, onFailure)
            else -> {
                val email = getEmailForUsername(identifier)
                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email, onSuccess, onFailure)
                } else {
                    _authState.value = AuthState.Error("No account found with that username")
                }
            }
        }
    }

    private fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth?.sendPasswordResetEmail(email)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
    }

    fun checkUsernameAvailability(username: String, onResult: (Boolean) -> Unit) {
        if (username.isEmpty()) {
            _authState.value = AuthState.Error("Username cannot be empty")
            onResult(false)
            return
        }

        Firebase.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error checking username")
                onResult(false)
            }
    }

    fun checkEmailAvailability(email: String, onResult: (Boolean) -> Unit) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            onResult(false)
            return
        }

        if (!isEmail(email)) {
            _authState.value = AuthState.Error("Invalid email format")
            onResult(false)
            return
        }

        Firebase.firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(true)
                } else {
                    _authState.value = AuthState.Error("Email already in use")
                    onResult(false)
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error checking email")
                onResult(false)
            }
    }

    companion object {
        var isPreviewMode = false
    }
}

