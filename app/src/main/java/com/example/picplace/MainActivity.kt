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
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.services.LocationTrackerService
import com.example.picplace.services.NearbyCheckService
import com.example.picplace.ui.navigation.Navigation
import com.example.picplace.ui.theme.PicPlaceTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val userViewModel : UserViewModel by viewModels()
        val placeViewModel : PlaceViewModel by viewModels()

        setContent {
            PicPlaceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Navigation(
                        modifier =  Modifier,
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        placeViewModel = placeViewModel
                    )
                    val authState = authViewModel.authState.observeAsState()

                    when(authState.value){
                        is AuthState.Authenticated -> {
                            if (!areLocationPermissionsGranted()) {
                                requestLocationPermissions()
                                if (areLocationPermissionsGranted()) {
                                    startLocationService()
                                    startNearbyCheckService()
                                }
                            } else {
                                startLocationService()
                                startNearbyCheckService()
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ActivityCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(
                                        this,
                                        arrayOf(
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ),
                                        0
                                    )
                                }
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            stopLocationService()
                            stopNearbyCheckService()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted && coarseLocationGranted
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
    internal fun startLocationService() {
        Intent(applicationContext, LocationTrackerService::class.java).apply {
            action = LocationTrackerService.ACTION_START
            startService(this)
        }
    }

    internal fun stopLocationService() {
        stopNearbyCheckService()
        Intent(applicationContext, LocationTrackerService::class.java).apply {
            action = LocationTrackerService.ACTION_STOP
            startService(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun startNearbyCheckService() {
        Intent(applicationContext, NearbyCheckService::class.java).apply {
            action = NearbyCheckService.ACTION_START
            startService(this)
        }
    }

    internal fun stopNearbyCheckService() {
        Intent(applicationContext, NearbyCheckService::class.java).apply {
            action = NearbyCheckService.ACTION_STOP
            startService(this)
        }
    }
}