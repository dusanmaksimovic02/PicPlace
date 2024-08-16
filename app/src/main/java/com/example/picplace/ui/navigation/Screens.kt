package com.example.picplace.ui.navigation

sealed class Screens(val screen: String) {
    data object Home: Screens("home")
    data object Register: Screens("register")
    data object Login: Screens("login")
    data object ForgotPassword: Screens("forgotPassword")
}