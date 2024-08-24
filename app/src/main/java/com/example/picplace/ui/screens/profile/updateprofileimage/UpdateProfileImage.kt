package com.example.picplace.ui.screens.profile.updateprofileimage

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.picplace.R
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.screens.register.components.MyModalBottomSheet
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UpdateProfileImage(
    navController: NavController,
    modifier: Modifier,
    userViewModel: UserViewModel
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tempUri = remember {
        mutableStateOf<Uri?>(null)
    }

    var newProfileImage by remember {
        mutableStateOf(Uri.EMPTY)
    }

    fun getTempUri(): Uri? {
        return try {
            val imagesDir = File(context.cacheDir, "images")
            imagesDir.mkdirs()
            val file = File.createTempFile(
                "image_" + System.currentTimeMillis().toString(),
                ".jpg",
                imagesDir
            )
            FileProvider.getUriForFile(
                context,
                context.getString(R.string.fileprovider),
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                it.let { uri ->
                    newProfileImage = uri
                }
            }
        }
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {isSaved ->
            if (isSaved) {
                tempUri.value?.let { uri ->
                    newProfileImage = uri
                }
            } else {
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tmpUri = getTempUri()
            tempUri.value = tmpUri
            takePhotoLauncher.launch(tempUri.value!!)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet){
        MyModalBottomSheet(
            onDismiss = {
                showBottomSheet = false
            },
            onTakePhotoClick = {
                showBottomSheet = false

                val permission = Manifest.permission.CAMERA
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                ) {
                    val tmpUri = getTempUri()
                    tempUri.value = tmpUri
                    takePhotoLauncher.launch(tempUri.value!!)
                } else {
                    cameraPermissionLauncher.launch(permission)
                }
            },
            onPhotoGalleryClick = {
                showBottomSheet = false
                imagePicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Change profile picture",
                color = Color(0xFF425980),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )

            Spacer(modifier = modifier.weight(1f))

            OutlinedButton(
                onClick = { showBottomSheet = true },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                border = BorderStroke(2.dp, Color(0xFF425980)),
                shape = RoundedCornerShape(13.dp)
            ) {
                Text(
                    text = "Upload/Take new profile photo",
                    color = Color(0xFF425980)
                )
            }

            newProfileImage?.let {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = it,
                        modifier = modifier.size(
                            200.dp
                        ),
                        contentDescription = "photo",
                    )
                }
            }

            Spacer(modifier = modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        userViewModel.changeProfilePicture(
                            newPictureUri = newProfileImage,
                            onSuccess = {
                                Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_LONG).show()
                                userViewModel.onLogin()
                                navController.popBackStack()
                            },
                            onFailure = { e ->
                                Toast.makeText(context, "Error while updating profile picture", Toast.LENGTH_LONG).show()
                                Log.e("Error while updating profile picture", e.message.toString())
                            }
                        )
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                border = BorderStroke(2.dp, Color(0xFF425980)),
                shape = RoundedCornerShape(13.dp),
                enabled = newProfileImage != Uri.EMPTY
            ) {
                Text(
                    text = "Update profile picture",
                    color = Color(0xFF425980)
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
fun PreviewUpdateImage() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        UpdateProfileImage(
            modifier = Modifier,
            navController = rememberNavController(),
            userViewModel = MockUserViewModel()
        )
    }
}