package com.example.picplace.ui.screens.leaderboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun LeaderboardScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 2
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Leaderboard screen",
                color = Color(0xFF425980)
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun LeaderboardPreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        LeaderboardScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel()
        )
    }
}