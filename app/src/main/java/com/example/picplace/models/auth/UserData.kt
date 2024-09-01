package com.example.picplace.models.auth

data class UserData (
    var id : String = "",
    var email: String = "",
    var username: String = "",
    var name: String = "",
    var surname: String = "",
    var phoneNumber: String = "",
    var imageUrl: String = "",
    var score: Int = 0
)