package com.example.picplace

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.services.LocationTrackerService
import com.example.picplace.ui.navigation.Navigation
import com.example.picplace.ui.theme.PicPlaceTheme

class MainActivity : ComponentActivity() {
    private var locationServiceIntent: Intent? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val userViewModel : UserViewModel by viewModels()

        setContent {
            PicPlaceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Navigation(
                        modifier =  Modifier,
                        authViewModel = authViewModel,
                        userViewModel = userViewModel
                    )
                    val authState = authViewModel.authState.observeAsState()

                    when(authState.value){
                        is AuthState.Authenticated -> {
                            if (!areLocationPermissionsGranted()) {
                                requestLocationPermissions()
                            } else {
                                startLocationService()
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            stopLocationService()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        stopLocationService()
    }

    private fun areLocationPermissionsGranted(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocationGranted && coarseLocationGranted && backgroundLocationGranted
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationService() {
        if (locationServiceIntent == null) {
            locationServiceIntent = Intent(applicationContext, LocationTrackerService::class.java)
            startForegroundService(locationServiceIntent)
        }
    }

    private fun stopLocationService() {
        locationServiceIntent?.let {
            stopService(it)
            locationServiceIntent = null
        }
    }
}