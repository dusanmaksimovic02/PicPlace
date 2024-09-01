package com.example.picplace.models.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.picplace.models.auth.UserData

class MockUserViewModel : UserViewModel() {

    private val _mockUserData = MutableLiveData<UserData?>().apply {
        value = UserData(
            username = "MockUser",
            name = "John",
            surname = "Doe",
            phoneNumber = "+1234567890",
            email = "mockuser@example.com",
            imageUrl = ""
        )
    }

    override val userData: LiveData<UserData?> = _mockUserData

    override suspend fun fetchUser (id : String) : UserData {
        return UserData(
            username = "MockUser",
            name = "John",
            surname = "Doe",
            phoneNumber = "+1234567890",
            email = "mockuser@example.com",
            imageUrl = ""
        )
    }
}