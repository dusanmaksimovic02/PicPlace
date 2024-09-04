package com.example.picplace.ui.screens.map

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.picplace.ui.components.CustomTextField
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
import kotlinx.coroutines.launch
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
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
    var placesToShow by remember {
        mutableStateOf<List<PlaceFirebase?>>(emptyList())
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
                placesToShow = fetchedPlaces
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
    var usernameFilter by remember {
        mutableStateOf<String?>("")
    }
    var nameFilter by remember {
        mutableStateOf<String?>("")
    }
    var startDateFilter by remember {
        mutableStateOf<Long?>(null)
    }
    var endDateFilter by remember {
        mutableStateOf<Long?>(null)
    }
    var radiusFilter by remember {
        mutableStateOf<Double?>(null)
    }
    var isFilterClicked by remember {
        mutableStateOf(false)
    }
    var isFiltered by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    var showStartDatePicker by remember {
        mutableStateOf(false)
    }
    var showEndDatePicker by remember {
        mutableStateOf(false)
    }
    var isUsernameFilterFocused by remember {
        mutableStateOf(false)
    }
    var isNameFilterFocused by remember {
        mutableStateOf(false)
    }
    var isRadiusFilterFocused by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current

    if (showStartDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedDate ->
                startDateFilter = selectedDate
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedDate ->
                endDateFilter = selectedDate
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 1
            )
        }
    ) { innerPadding ->
        if(isFilterClicked) {
            Column (
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(10.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                IconButton(
                    onClick = {
                        isFilterClicked = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Enter values for search",
                    modifier = modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xff425980)
                )

                Spacer(modifier = modifier.weight(0.5f))
                
                CustomTextField(
                    value = if (usernameFilter != null) usernameFilter!! else "",
                    onValueChange = {
                        usernameFilter = it
                    },
                    label = "Filter by username",
                    isFocused = isUsernameFilterFocused,
                    onFocusChange = {
                        isUsernameFilterFocused = it
                    },
                    imageVector =  Icons.Outlined.Person
                )

                CustomTextField(
                    value = if (nameFilter != null) nameFilter!! else "",
                    onValueChange = {
                        nameFilter = it
                    },
                    label = "Filter by Place Name",
                    isFocused = isNameFilterFocused,
                    onFocusChange = {
                        isNameFilterFocused = it
                    },
                    imageVector =  Icons.Outlined.Title
                )

                CustomTextField(
                    value = radiusFilter?.toString() ?: "",
                    onValueChange = {
                        radiusFilter = it.toDoubleOrNull()
                    },
                    label = "Radius (meters)",
                    isFocused = isRadiusFilterFocused,
                    onFocusChange = {
                        isRadiusFilterFocused = it
                    },
                    imageVector =  Icons.Outlined.Title
                )
                
                OutlinedButton(
                    onClick = {
                        showStartDatePicker = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    border = BorderStroke(2.dp, Color(0xff425980)),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        text = "Select Start Date: ${startDateFilter?.let { DateFormat.getDateInstance().format(it) } ?: "Not selected"}",
                        color = Color(0xff425980)
                    )
                }

                OutlinedButton(
                    onClick = {
                        showEndDatePicker = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    border = BorderStroke(2.dp, Color(0xff425980)),
                    shape = RoundedCornerShape(13.dp)
                    ) {
                    Text(
                        text = "Select End Date: ${endDateFilter?.let { DateFormat.getDateInstance().format(it) } ?: "Not selected"}",
                        color = Color(0xff425980)
                    )
                }

                Spacer(modifier = modifier.weight(1f))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            placeViewModel.getFilteredPlaces(
                                username = usernameFilter?.ifBlank { null },
                                name = nameFilter?.ifBlank { null },
                                startDate = startDateFilter,
                                endDate = endDateFilter,
                                radius = radiusFilter,
                                currentLocation = currentLocation,
                                onSuccess = { fetchedPlaces ->
                                    placesToShow = fetchedPlaces
                                    isFilterClicked = false
                                    isFiltered = true
                                },
                                onFailure = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = modifier
                        .clip(RoundedCornerShape(7.dp))
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff425980),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(13.dp),
                    enabled = !usernameFilter.isNullOrEmpty() || !nameFilter.isNullOrEmpty() || radiusFilter != null || (startDateFilter != null && endDateFilter != null)
                ) {
                    Text("Apply Filters")
                }
            }

        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = properties,
                    uiSettings = mapUiSettings,
                    onMapLongClick = { latLng ->
                        navController.navigate("${Screens.AddPlaceScreen.screen}/${latLng.latitude.toFloat()}/${latLng.longitude.toFloat()}")
                    }
                ) {
                    if (placesToShow.isNotEmpty()) {
                        placesToShow.forEach { place ->
                            val markerIcon =
                                BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)

                            Marker(
                                state = rememberMarkerState(position = place?.latLng!!.toLatLng()),
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

                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 55.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(13.dp))
                ) {
                    IconButton(
                        onClick = {
                            isFilterClicked = true
                        },
                        modifier = modifier
                            .background(Color.White)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Search",
                            tint = Color(0xff425980)
                        )
                    }
                }

                if (isFiltered) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 110.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(13.dp))
                    ) {
                        IconButton(
                            onClick = {
                                usernameFilter = null
                                nameFilter = null
                                startDateFilter = null
                                endDateFilter = null
                                radiusFilter = null
                                coroutineScope.launch {
                                    placeViewModel.getPlaces(
                                        onSuccess = { fetchedPlaces ->
                                            placesToShow = fetchedPlaces
                                        },
                                        onFailure = { errorMessage ->
                                            Log.e("MapScreen", "Error fetching places: $errorMessage")
                                        }
                                    )
                                }
                                isFiltered = false
                            },
                            modifier = modifier
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = "cancel",
                                tint = Color.Red
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
                        containerColor = Color.White
                    ),
                    enabled = isLocationServiceRunning
                ) {
                    Text(
                        text = "Add Place",
                        color = Color(0xff425980)
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}