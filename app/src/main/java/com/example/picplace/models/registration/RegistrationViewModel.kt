package com.example.picplace.models.registration

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {
    var username by mutableStateOf(RegisterViewModelData())
    var email by mutableStateOf(RegisterViewModelData())
    var password by mutableStateOf(RegisterViewModelData())
    var name by mutableStateOf("")
    var surname by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var photoUri by mutableStateOf<Uri?>(null)
}

class RegisterViewModelData {
    var value by mutableStateOf("")
    var isValid by mutableStateOf(false)

    init {
        value = ""
        isValid = false
    }
}