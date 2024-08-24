package com.example.picplace.ui.screens.profile.fullimage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picplace.models.user.UserViewModel

@Composable
fun FullImageScreen(userViewModel: UserViewModel, onDismiss: () -> Unit) {
    val userData = userViewModel.userData.observeAsState()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userData.value?.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Full-Screen Profile Picture",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}