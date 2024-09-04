package com.example.picplace.ui.screens.placestable

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.models.place.MockPlaceViewModel
import com.example.picplace.models.place.PlaceFirebase
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesTableScreen(
    modifier: Modifier,
    navController: NavController,
    placeViewModel: PlaceViewModel
) {
    var places by remember {
        mutableStateOf<List<PlaceFirebase>>(emptyList())
    }

    LaunchedEffect(Unit) {
        placeViewModel.getPlaces(
            onSuccess = { fetchedPlaces ->
                places = fetchedPlaces
            },
            onFailure = { errorMessage ->
                Log.e("PlacesTableScreen", "Error fetching places: $errorMessage")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Places Table",
                        color = Color(0xFF425980),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
            )
        },
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
                .padding(innerPadding)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlacesTable(
                places = places,
                navController = navController
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PlacesTablePreview() {
    isPreviewMode = true
    PicPlaceTheme {
        PlacesTableScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            placeViewModel = MockPlaceViewModel()
        )
    }
}

@Composable
fun PlacesTable(
    places: List<PlaceFirebase>,
    navController: NavController
) {
    val headerBackgroundColor = Color(0xff425980)
    val headerTextColor = MaterialTheme.colorScheme.onSurface
    val cellBackgroundColor = MaterialTheme.colorScheme.surface
    val cellTextColor = MaterialTheme.colorScheme.onSurface
    val gson = remember { Gson() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
        ) {
            TableHeaderCell(
                text = "No",
                textColor = headerTextColor,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(5.dp)
            )
            TableHeaderCell(
                text = "Name",
                textColor = headerTextColor,
                modifier = Modifier
                    .weight(2f)
                    .padding(5.dp)
            )
            TableHeaderCell(
                text = "Latitude",
                textColor = headerTextColor,
                modifier = Modifier
                    .weight(2f)
                    .padding(5.dp)
            )
            TableHeaderCell(
                text = "Longitude",
                textColor = headerTextColor,
                modifier = Modifier.weight(2f)
            )
            TableHeaderCell(
                text = "Description",
                textColor = headerTextColor,
                modifier = Modifier
                    .weight(3f)
                    .padding(5.dp)
            )
            TableHeaderCell(
                text = "Likes",
                textColor = headerTextColor,
                modifier = Modifier
                    .weight(1.2f)
                    .padding(5.dp)
            )
        }

        places.forEachIndexed { index, place ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) cellBackgroundColor else Color.Transparent)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .clickable {
                        val placeJson = Uri.encode(gson.toJson(place))

                        navController.navigate("${Screens.ViewPlaceScreen.screen}/$placeJson")
                    }
            ) {
                TableCell(
                    text = (index + 1).toString(),
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(5.dp)
                )
                TableCell(
                    text = place.name,
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(2f)
                        .padding(5.dp)
                )
                TableCell(
                    text = place.latLng.toLatLng().latitude.toString(),
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(2f)
                        .padding(5.dp)
                )
                TableCell(
                    text = place.latLng.toLatLng().longitude.toString(),
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(2f)
                        .padding(5.dp)
                )
                TableCell(
                    text = place.description,
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(3f)
                        .padding(5.dp)
                )
                TableCell(
                    text = place.likes.toString(),
                    color = cellTextColor,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(5.dp)
                )
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, textColor: Color, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TableCell(text: String, color: Color, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}