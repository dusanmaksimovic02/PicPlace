package com.example.picplace.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import com.example.picplace.utils.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }
    val locationClient = remember {
        DefaultLocationClient(context, LocationServices.getFusedLocationProviderClient(context))
    }
    val mapUiSettings = MapUiSettings()
    val properties by remember {
        mutableStateOf(MapProperties(
            mapType= MapType.HYBRID,
            isIndoorEnabled = true,
            isBuildingEnabled = true,
            isTrafficEnabled = true,
            isMyLocationEnabled = true
        ))
    }

    LaunchedEffect(Unit) {
        locationClient.getLocationUpdates(10000L).collect { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
        }
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    val defaultLocation = LatLng(43.321445, 21.896104)
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(currentLocation) {
        val userLocation = currentLocation ?: defaultLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 1
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState= cameraPositionState,
                properties = properties,
                uiSettings= mapUiSettings
            )

        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun MapPreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        MapScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel()
        )
    }
}
