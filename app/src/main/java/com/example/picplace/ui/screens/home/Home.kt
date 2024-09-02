package com.example.picplace.ui.screens.home

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picplace.R
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.place.MockPlaceViewModel
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    placeViewModel: PlaceViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val topUsers = userViewModel.topUsers.observeAsState()
    val topPlaces = placeViewModel.topPlaces.observeAsState()
    val gson = remember { Gson() }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    LaunchedEffect (Unit) {
        userViewModel.updateTopUsers()
        placeViewModel.updateTopPlaces()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_logoo),
                        contentDescription = "logo",
                    )
                },
                title = {
                    Text(
                        text = "PicPlace",
                        color = Color(0xFF425980)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 0
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to PicPlace, your go-to app for discovering the best photo spots in the city!",
                color = Color(0xFF425980),
                modifier = Modifier.padding(10.dp),
                fontSize = 25.sp,
            )

            Text(
                text = "Besides finding the perfect photo spots, you can also have fun by liking, commenting, and filling out polls about the locations. Each interaction earns you points to improve your ranking on the leaderboard:\n" +
                        "- Liking: 2 points\n" +
                        "- Commenting: 5 points\n" +
                        "- Filling out polls: 10 points\n" +
                        "- Adding a new spot: 25 points",
                color = Color(0xFF425980),
                modifier = Modifier.padding(10.dp),
                fontSize = 21.sp,
            )

            Text(
                text = "Top 5 places",
                color = Color(0xFF425980),
                modifier = Modifier.padding(10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                topPlaces.value?.forEach { place ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val placeJson = Uri.encode(gson.toJson(place))

                                navController.navigate("${Screens.ViewPlaceScreen.screen}/$placeJson")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                shape = RectangleShape,
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(10.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(place.imageUrls[0])
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(RectangleShape)
                                )
                            }

                            Column (
                                modifier = modifier
                                    .fillMaxWidth(),
//                                    .size(90.dp)
//                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = place.name,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(text = "Latitude - ${place.latLng.toLatLng().latitude}")

                                Text(text = "Longitude - ${place.latLng.toLatLng().longitude}")

                                Text(text = place.description)
                            }
                        }
                    }
                }
            }

            Text(
                text = "Top 5 users",
                color = Color(0xFF425980),
                modifier = Modifier.padding(10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                topUsers.value?.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Card(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(10.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                )
                            }

                            Column (
                                modifier = modifier
                                    .fillMaxWidth()
                                    .size(90.dp)
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = modifier
                                        .fillMaxWidth(),
                                ) {
                                    Text(
                                        text = user.username,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = modifier.weight(1f))
                                    
                                    Text(text = "Score: ${user.score}")
                                }
                            }
                        }
                    }
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
fun HomePreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        HomeScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel(),
            placeViewModel = MockPlaceViewModel()
        )
    }
}