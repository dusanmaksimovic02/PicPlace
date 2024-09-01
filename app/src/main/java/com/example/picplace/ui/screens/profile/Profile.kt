package com.example.picplace.ui.screens.profile

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picplace.MainActivity
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.services.LocationTrackerService
import com.example.picplace.ui.navigation.BottomNavigationBar
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val userData = userViewModel.userData.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    var showDeleteAccountDialog by remember {
        mutableStateOf(false)
    }
    val skipPartiallyExpanded by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val context = LocalContext.current
    val edgeToEdgeEnabled by remember {
        mutableStateOf(false)
    }
    val windowInsets = if (edgeToEdgeEnabled)
        WindowInsets(0) else BottomSheetDefaults.windowInsets
    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    var isLocationTrackerAllowed by remember {
        mutableStateOf(if(isPreviewMode) {
            true
        } else {
            isServiceRunning(context, LocationTrackerService::class.java)
        })
    }
    var isSendNotificationAllowed by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.updateCurrentUser()
    }

    if(showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet = false
            },
            windowInsets = windowInsets
        ) {
            BottomSheetContent(
                onViewPicture = {
                    showBottomSheet = false
                    navController.navigate(Screens.FullImageScreen.screen)
                },
                onUpdatePicture = {
                    showBottomSheet = false
                    navController.navigate(Screens.UpdateProfileImage.screen)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color(0xFF425980)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        showDeleteAccountDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Sign out",
                            tint = Color.Red
                        )
                    }
                    IconButton(onClick = {
                        authViewModel.signOut(userViewModel)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Sign out",
                            tint = Color(0xFF425980)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),

            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = 3
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    modifier = modifier
                        .size(120.dp)
                        .padding(10.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userData.value?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .clip(CircleShape)
                            .clickable {
                                showBottomSheet = true
                            }
                    )
                }

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    modifier = modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = userData.value!!.username,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF425980)
                    )

                    Text(
                        text = "Score:  ${userData.value!!.score}",
                        fontSize = 20.sp,
                        color = Color(0xFF425980)
                    )
                }
            }

            Column(
                modifier = modifier
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Full name: ${userData.value!!.name} ${userData.value!!.surname}",
                    color = Color(0xFF425980)
                )

                Text(
                    text = "E-mail: ${userData.value!!.email}",
                    color = Color(0xFF425980)
                )

                Text(
                    text = "Phone number: ${userData.value!!.phoneNumber}",
                    color = Color(0xFF425980)
                )
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Screens.EditUser.screen) },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                border = BorderStroke(2.dp, Color(0xFF425980)),
                shape = RoundedCornerShape(13.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    color = Color(0xFF425980)
                )
            }
            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    userViewModel.deleteAccount(
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Account is deleted successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            authViewModel.setUnauthenticatedState() },
                                        onFailure = {
                                            Toast.makeText(
                                                context,
                                                "Error while deleting account",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                                showDeleteAccountDialog = false
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        } },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteAccountDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )  {
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow tracking location",
                        color = Color(0xff425980),
                        modifier = modifier
                            .weight(1f)
                    )

                    Switch(
                        checked = isLocationTrackerAllowed,
                        onCheckedChange = { isChecked ->
                            isLocationTrackerAllowed = isChecked
                            if (isChecked) {
                                (context as MainActivity).startLocationService()
                            } else {
                                (context as MainActivity).stopLocationService()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF425980),
                        ),
                        thumbContent = if (isLocationTrackerAllowed) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )  {
                    Text(
                        text = "Allow sending notification about nearby places",
                        color = Color(0xff425980),
                        modifier = modifier
                            .weight(1f)
                    )
                    Switch(
                        checked = isSendNotificationAllowed,
                        onCheckedChange = { isSendNotificationAllowed = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF425980),
                        ),
                        thumbContent = if (isSendNotificationAllowed) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

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
fun ProfilePreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        ProfileScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel()
        )
    }
}

@Composable
fun BottomSheetContent(
    onViewPicture: () -> Unit,
    onUpdatePicture: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Profile Picture Options",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ListItem(
            headlineContent = { Text("View Picture") },
            modifier = Modifier.clickable { onViewPicture() }
        )
        ListItem(
            headlineContent = { Text("Update Picture") },
            modifier = Modifier.clickable { onUpdatePicture() }
        )
    }
}

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE).any {
        it.service.className == serviceClass.name
    }
}