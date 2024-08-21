package com.example.picplace.models.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MockAuthViewModel : AuthViewModel() {
    private val _mockAuthState = MutableLiveData<AuthState>()

    init {
        _mockAuthState.value = AuthState.Unauthenticated
    }

    override val authState: LiveData<AuthState>
        get() = _mockAuthState

    override fun register(
        username: String,
        password: String,
        email: String,
        name: String,
        surname: String,
        phoneNumber: String,
        photoUri: Uri,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        _mockAuthState.value = AuthState.Authenticated
        onSuccess()
    }

    override suspend fun login(username: String, password: String) {
        _mockAuthState.value = AuthState.Error("Login failed")
    }
}
