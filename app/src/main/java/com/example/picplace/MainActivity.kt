package com.example.picplace

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.navigation.Navigation
import com.example.picplace.ui.theme.PicPlaceTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val userViewModel : UserViewModel by viewModels()
        setContent {
            PicPlaceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Navigation(modifier =  Modifier, authViewModel = authViewModel, userViewModel = userViewModel)
                }
            }
        }
    }
}

