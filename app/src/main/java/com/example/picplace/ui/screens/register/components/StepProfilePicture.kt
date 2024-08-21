package com.example.picplace.ui.screens.register.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.picplace.R
import java.io.File

@Composable
fun StepProfilePicture(
    onBackStep: () -> Unit,
    authViewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier,
    registrationViewModel: RegistrationViewModel,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    val tempUri = remember {
        mutableStateOf<Uri?>(null)
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
                    registrationViewModel.photoUri = uri
                }
            }
        }
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {isSaved ->
            if (isSaved) {
                tempUri.value?.let { uri ->
                    registrationViewModel.photoUri = uri
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedButton(
            onClick = { showBottomSheet = true },
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Upload/Take photo"
            )
        }

        registrationViewModel.photoUri?.let {
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

        Row(
            modifier = modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = onBackStep
            ) {
                Text(
                    text = "Back"
                )
            }
        }

        Button(
            onClick = {
                authViewModel.register(
                    username = registrationViewModel.username.value,
                    password = registrationViewModel.password.value,
                    email = registrationViewModel.email.value,
                    name = registrationViewModel.name,
                    surname = registrationViewModel.surname,
                    phoneNumber = registrationViewModel.phoneNumber,
                    photoUri = registrationViewModel.photoUri!!,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Success register, please verify your email",
                            Toast.LENGTH_LONG).show()
                        navController.navigate(Screens.Home.screen)
                    },
                    onFailure = {
                        Toast.makeText(
                            context,
                            "Register failed",
                            Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF425980)
            ),
            enabled = registrationViewModel.photoUri != null && authState.value != AuthState.Loading
        ) {
            Text(
                text = "Register",
                color = Color.White,
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun PreviewStepProfilePicture() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        StepProfilePicture(
            onBackStep = {},
            authViewModel = MockAuthViewModel(),
            modifier = Modifier,
            navController = rememberNavController(),
            registrationViewModel = RegistrationViewModel()
        )
    }
}