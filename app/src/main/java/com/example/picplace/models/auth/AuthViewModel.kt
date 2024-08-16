package com.example.picplace.models.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
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

    open fun register(username: String, password: String, email: String, name: String, surname: String, phoneNumber: String, onSuccess: () -> Unit, onFailure : () -> Unit) {
        _authState.value = AuthState.Loading
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener{task->
                if (task.isSuccessful){
                    uploadProfilePicture()
                    saveUserData(
                        username = username,
                        email = email,
                        name = name,
                        surname = surname,
                        phoneNumber = phoneNumber,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    private fun saveUserData(username: String, email: String, name: String, surname: String, phoneNumber: String, onSuccess: () -> Unit, onFailure : () -> Unit) {
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
            phoneNumber
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

    private fun uploadProfilePicture() {

    }

    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email,password)
            ?.addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
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

    companion object {
        var isPreviewMode = false
    }
}

