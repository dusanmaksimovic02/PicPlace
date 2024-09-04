package com.example.picplace.ui.navigation

sealed class Screens(val screen: String) {
    data object Home: Screens("home")
    data object Register: Screens("register")
    data object Login: Screens("login")
    data object ForgotPassword: Screens("forgotPassword")
    data object Leaderboard: Screens("leaderboard")
    data object Map: Screens("map")
    data object Profile: Screens("profile")
    data object EditUser: Screens("editUser")
    data object UpdateProfileImage: Screens("updateProfilePicture")
    data object FullImageScreen: Screens("fullImageScreen")
    data object AddPlaceScreen: Screens("addPlace")
    data object ViewPlaceScreen: Screens("viewPlace")
    data object PlacesTable: Screens("placesTable")
}