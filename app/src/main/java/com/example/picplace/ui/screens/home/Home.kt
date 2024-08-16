package com.example.picplace.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun HomeScreen(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home page",
            color = Color(0xFF425980)
        )

        Spacer(
            modifier = modifier
                .height(15.dp)
        )

        Button(
            onClick = {
                authViewModel.signOut()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF425980)
            )
        ) {
            Text(
                text = "Sign out"
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun HomePreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        HomeScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel()
        )
    }
}