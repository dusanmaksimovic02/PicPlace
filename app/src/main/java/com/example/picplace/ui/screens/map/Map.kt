package com.example.picplace.ui.screens.map

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.picplace.R
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.place.MockPlaceViewModel
import com.example.picplace.models.place.PlaceFirebase
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.services.LocationTrackerService
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.screens.profile.isServiceRunning
import com.example.picplace.ui.theme.PicPlaceTheme
import com.example.picplace.utils.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    placeViewModel: PlaceViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }
    val isLocationServiceRunning by remember {
        mutableStateOf(if (isPreviewMode) {
            true
        }else {
            isServiceRunning(context, LocationTrackerService::class.java)
        })
    }
    var places by remember {
        mutableStateOf<List<PlaceFirebase>>(emptyList())
    }

    if (isLocationServiceRunning) {
        val locationClient = remember {
            DefaultLocationClient(context, LocationServices.getFusedLocationProviderClient(context))
        }

        LaunchedEffect(Unit) {
            locationClient.getLocationUpdates(10000L).collect { location ->
                currentLocation = LatLng(location.latitude, location.longitude)
            }
        }
    }

    LaunchedEffect(Unit) {
        placeViewModel.getPlaces(
            onSuccess = { fetchedPlaces ->
                places = fetchedPlaces
            },
            onFailure = { errorMessage ->
                Log.e("MapScreen", "Error fetching places: $errorMessage")
            }
        )
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    val defaultLocation = LatLng(43.321445, 21.896104)
    val cameraPositionState = rememberCameraPositionState()

    var userLocation by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }

    LaunchedEffect(currentLocation) {
        userLocation = currentLocation ?: defaultLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 16f)
    }

    val gson = remember { Gson() }

    val mapUiSettings = MapUiSettings()
    val properties by remember {
        mutableStateOf(MapProperties(
            mapType= MapType.HYBRID,
            isMyLocationEnabled = isLocationServiceRunning
        ))
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 1
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState= cameraPositionState,
                properties = properties,
                uiSettings= mapUiSettings,
                onMapLongClick = { latLng ->
                    navController.navigate("${Screens.AddPlaceScreen.screen}/${latLng.latitude.toFloat()}/${latLng.longitude.toFloat()}")
                }
            ) {
                if (places.isNotEmpty()) {
                    places.forEach { place ->
                        val markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)

                        Marker(
                            state = rememberMarkerState(position = place.latLng.toLatLng()),
                            title = place.name,
                            snippet = place.description,
                            icon = markerIcon,
                            onClick = {
                                val placeJson = Uri.encode(gson.toJson(place))

                                navController.navigate("${Screens.ViewPlaceScreen.screen}/$placeJson")

                                true
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate("${Screens.AddPlaceScreen.screen}/${userLocation.latitude.toFloat()}/${userLocation.longitude.toFloat()}")
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 25.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff425980)
                )
            ) {
                Text(
                    text = "Add Place",
                    color = Color.White
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MapPreview() {
    isPreviewMode = true
    PicPlaceTheme {
        MapScreen(
            modifier = Modifier,
            navController = rememberNavController(),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel(),
            placeViewModel = MockPlaceViewModel()
        )
    }
}