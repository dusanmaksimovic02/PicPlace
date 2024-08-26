package com.example.picplace.ui.screens.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picplace.R
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val users = listOf(
        User("Alice", 1200, false),
        User("Bob", 1150, false),
        User( "Charlie", 1100, true),
        User("Dave", 1500, false),
        User("Dave", 2360, false),
        User("Dave", 150, false),
        User("Dave", 2, false),
        User("Dave", 1342, false)
    ).sortedByDescending { it.score }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Leaderboard",
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn {
                itemsIndexed(users) { index, user ->
                    LeaderboardItemRow(user = user, rank = index + 1)
                }
            }
        }
    }
}

data class User(
    val name: String,
    val score: Int,
    val isCurrentUser: Boolean,
    val profileImage: String = "https://firebasestorage.googleapis.com/v0/b/pic-place-4e026.appspot.com/o/profile_pictures%2FksqjFUbue0TetszjzUvsJLRQ9mI3.jpg?alt=media&token=3bb1b959-fd0e-4b97-b247-8d443cbcfca1"

)

@Composable
fun LeaderboardItemRow(user: User, rank: Number) {
    val backgroundColor = if (user.isCurrentUser) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) else Color.Transparent
    val textColor = if (user.isCurrentUser) Color(0xFF425980) else Color.Black
    val borderStroke: BorderStroke = if(user.isCurrentUser) {
        BorderStroke(3.dp, Color(0xFF425980))
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .border(borderStroke, RoundedCornerShape(13.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(backgroundColor)
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
                        .data(user.profileImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Score: ${user.score}")
                    }

                    Row {
                        if (rank.toInt() < 4)
                            Icon(
                                painter = painterResource(id = R.drawable.ic_crown),
                                contentDescription = "",
                                tint = when (rank) {
                                    1 -> Color(0xffFFD700)
                                    2 -> Color(0xffc0c0c0)
                                    3 -> Color(0xffCD7F32)
                                    else -> Color.Black
                                }
                            )
                        Text(
                            text = "Rank $rank",
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }
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