package com.example.picplace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.picplace.ui.screens.home.HomeScreen
import com.example.picplace.ui.screens.leaderboard.LeaderboardScreen
import com.example.picplace.ui.screens.login.LoginScreen
import com.example.picplace.ui.screens.map.MapScreen
import com.example.picplace.ui.screens.profile.ProfileScreen
import com.example.picplace.ui.screens.register.RegisterScreen

@Composable
fun Navigation(modifier: Modifier, authViewModel: AuthViewModel){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = (Screens.Login.screen), builder = {
        composable(Screens.Login.screen) {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable(Screens.Register.screen) {
            RegisterScreen(modifier, navController, authViewModel)
        }
        composable(Screens.Home.screen) {
            HomeScreen(modifier, navController, authViewModel)
        }
        composable(Screens.ForgotPassword.screen) {
            ForgotPasswordScreen(modifier, navController, authViewModel)
        }
        composable(Screens.Leaderboard.screen) {
            LeaderboardScreen(modifier, navController, authViewModel)
        }
        composable(Screens.Map.screen) {
            MapScreen(modifier, navController, authViewModel)
        }
        composable(Screens.Profile.screen) {
            ProfileScreen(modifier, navController, authViewModel)
        }
    })
}