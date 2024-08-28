package com.example.picplace.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.screens.profile.edituser.EditUserScreen
import com.example.picplace.ui.screens.login.forgotpassword.ForgotPasswordScreen
import com.example.picplace.ui.screens.profile.fullimage.FullImageScreen
import com.example.picplace.ui.screens.home.HomeScreen
import com.example.picplace.ui.screens.leaderboard.LeaderboardScreen
import com.example.picplace.ui.screens.login.LoginScreen
import com.example.picplace.ui.screens.map.MapScreen
import com.example.picplace.ui.screens.map.addplace.AddPlaceScreen
import com.example.picplace.ui.screens.map.addplace.ViewPlaceScreen
import com.example.picplace.ui.screens.profile.ProfileScreen
import com.example.picplace.ui.screens.profile.updateprofileimage.UpdateProfileImage
import com.example.picplace.ui.screens.register.RegisterScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
){
    val navController = rememberNavController()

    val startDestination = if (authViewModel.authState.value is AuthState.Authenticated) {
        Screens.Home.screen
    } else {
        Screens.Login.screen
    }

    NavHost(navController = navController, startDestination = startDestination, builder = {
        composable(Screens.Login.screen) {
            LoginScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.Register.screen) {
            RegisterScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.Home.screen) {
            HomeScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.ForgotPassword.screen) {
            ForgotPasswordScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screens.Leaderboard.screen) {
            LeaderboardScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.Map.screen) {
            MapScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.Profile.screen) {
            ProfileScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.EditUser.screen) {
            EditUserScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }
        composable(Screens.UpdateProfileImage.screen){
            UpdateProfileImage(
                navController = navController,
                modifier = modifier,
                userViewModel = userViewModel
            )
        }
        composable(Screens.FullImageScreen.screen) {
            FullImageScreen(
                userViewModel = userViewModel,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.AddPlaceScreen.screen) {
            AddPlaceScreen(
                modifier = modifier
            )
        }
        composable(Screens.ViewPlaceScreen.screen) {
            ViewPlaceScreen(
                modifier = modifier
            )
        }
    })
}