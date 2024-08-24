package com.example.picplace.models.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.picplace.models.user.UserViewModel

class MockAuthViewModel : AuthViewModel() {
    private val _mockAuthState = MutableLiveData<AuthState>()

    init {
        _mockAuthState.value = AuthState.Unauthenticated
    }

    override val authState: LiveData<AuthState>
        get() = _mockAuthState

    override suspend fun register(
        username: String,
        password: String,
        email: String,
        name: String,
        surname: String,
        phoneNumber: String,
        photoUri: Uri,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        userViewModel: UserViewModel
    ) {
        _mockAuthState.value = AuthState.Authenticated
        onSuccess()
    }

    override suspend fun login(username: String, password: String, userViewModel: UserViewModel) {
        _mockAuthState.value = AuthState.Error("Login failed")
    }
}
