package com.example.picplace.ui.screens.map.viewplace

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.models.auth.UserData
import com.example.picplace.models.place.MockPlaceViewModel
import com.example.picplace.models.place.PlaceFirebase
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun ViewPlaceScreen(
    modifier: Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
    place: PlaceFirebase?,
    placeViewModel: PlaceViewModel,
) {
    val currentUser = userViewModel.userData.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var user by remember {
        mutableStateOf<UserData?>(UserData())
    }
    val selectedOptions = remember {
        mutableStateListOf<Int?>().apply {
            if (place != null) {
                addAll(List(place.poll.size) { null })
            }
        }
    }
    val isSubmitEnabled = remember {
        derivedStateOf { selectedOptions.all { it != null } }
    }
    var isUserTakePoll by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (!isPreviewMode) {
            coroutineScope.launch {
                try {
                    user = userViewModel.fetchUser(place!!.userId)
                    isUserTakePoll = placeViewModel.isUserTakePoll(place.id, currentUser.value!!.id)
                } catch (e: Exception) {
                    Log.e("ViewPlaceScreen", "Error fetching user data: ${e.message}")
                }
            }
        } else {
            user = UserData(
                username = "MockUser",
                name = "John",
                surname = "Doe",
                phoneNumber = "+1234567890",
                email = "mockuser@example.com",
                imageUrl = ""
            )
        }
    }

    var isLiked by remember {
        mutableStateOf(currentUser.value?.let { place?.likedBy?.contains(it.id) } == true)
    }
    var likeCount by remember {
        mutableIntStateOf(place?.likes ?: 0)
    }
    var newComment by remember {
        mutableStateOf("")
    }
    var isNewCommentFocused by remember {
        mutableStateOf(false)
    }
    var comments by remember {
        mutableStateOf(place?.comments ?: listOf())
    }
    val focusManager = LocalFocusManager.current

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(10.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Row (
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (place?.userId == currentUser.value?.id)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            placeViewModel.deletePlace(
                                placeId = place!!.id,
                                onSuccess = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = modifier.width(130.dp)
                ) {
                    Row (
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delete place",
                            color = Color.Red
                        )

                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete place",
                            tint = Color.Red
                        )
                    }
                }
            }

            Row (
                modifier = modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Card(
                    shape = CircleShape,
                    modifier = modifier
                        .size(60.dp)
                        .padding(10.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .clip(CircleShape)
                    )
                }

                (if (user != null) user?.username else "")?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xff425980),
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = modifier.weight(1f))

                Text(
                    text = Date(place!!.createdAt).toLocaleString()
                )
            }

            Text(
                text = place!!.name,
                modifier = modifier
                    .padding(10.dp),
                fontSize = 20.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(10.dp),
                modifier = modifier
                    .height(300.dp)
            ) {
                items(place.imageUrls) { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isLiked) {
                        likeCount--
                        isLiked = false
                        coroutineScope.launch {
                            currentUser.value?.let {
                                placeViewModel.unlikePlace(place.id, it.id)
                            }
                        }
                    } else {
                        likeCount++
                        isLiked = true
                        coroutineScope.launch {
                            currentUser.value?.let {
                                placeViewModel.likePlace(place.id, it.id)
                            }
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }

                Text(
                    text = likeCount.toString(),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = place.description
            )

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Card(
                            shape = CircleShape,
                            modifier = modifier
                                .size(60.dp)
                                .padding(10.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(comment.profilePictureUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = modifier
                                    .clip(CircleShape)
                            )
                        }

                        Column(
                            modifier = modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = comment.userName,
                                )

                                Spacer(modifier = modifier.weight(1f))

                                Text(
                                    text = Date(comment.timestamp).toLocaleString(),
                                )

                                if (comment.userId == currentUser.value?.id ) {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            currentUser.value?.let { placeViewModel.removeComment(place.id, comment.id, it.id) }
                                            comments =
                                                comments.filterNot { it.id == comment.id }
                                        }
                                        isNewCommentFocused = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Cancel,
                                            contentDescription = "Delete comment",
                                        )
                                    }
                                }
                            }

                            Text(
                                text = comment.content,
                                modifier = modifier
                                    .padding(horizontal = 25.dp)
                            )
                        }
                    }

                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = modifier.weight(1f)
                ) {
                    CustomTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = "Add a comment...",
                        modifier = modifier,
                        isFocused = isNewCommentFocused,
                        onFocusChange = {
                            isNewCommentFocused = it
                        },
                        imageVector = Icons.AutoMirrored.Outlined.Comment
                    )
                }

                IconButton(onClick = {
                    if (newComment.isNotBlank()) {
                        coroutineScope.launch {
                            currentUser.value?.let {
                                placeViewModel.addComment(
                                    placeId = place.id,
                                    userId = it.id,
                                    userName = currentUser.value!!.username,
                                    comment = newComment,
                                    profilePictureUrl = currentUser.value!!.imageUrl
                                )
                            }

                            placeViewModel.getPlaceById(place.id) { updatedPlace ->
                                comments = updatedPlace?.comments ?: listOf()
                            }

                            newComment = ""
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send comment"
                    )
                }
            }

            if (place.poll.isNotEmpty()) {
                if (place.userId == currentUser.value?.id || isUserTakePoll) {
                    Text(
                        text = "Poll Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )

                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        place.pollStatistics.forEachIndexed() { index, stat ->
                            Text(
                                text = "${index + 1}. ${stat.question}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xff425980)
                            )

                            stat.votesCount.forEach { (option, count) ->
                                val totalVotes = stat.votesCount.values.sum()
                                val percentage = if (totalVotes > 0) (count * 100f / totalVotes) else 0f

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = option,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp,
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(2f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.LightGray)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(percentage / 100f)
                                                .background(Color(0xff425980))
                                        )
                                    }

                                    Text(
                                        text = "${percentage.toInt()}%",
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .width(35.dp),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Take a poll",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )

                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        place.poll.forEachIndexed { index, pollObjects ->
                            Text(
                                text = "${index + 1}. ${pollObjects.question}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xff425980)
                            )

                            Column(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                pollObjects.options.forEachIndexed { optionIndex, option ->
                                    Row(
                                        modifier = modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedOptions[index] == optionIndex,
                                            onClick = {
                                                selectedOptions[index] = optionIndex
                                            }
                                        )
                                        Text(
                                            text = option,
                                            modifier = Modifier.padding(start = 8.dp),
                                            fontSize = 16.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                placeViewModel.submitPoll(
                                    placeId = place.id,
                                    userId = currentUser.value!!.id,
                                    pollResults = selectedOptions,
                                    onSuccess = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        coroutineScope.launch {
                                            placeViewModel.updatePollStatistics(placeId = place.id)
                                        }
                                        isUserTakePoll = true
                                    },
                                    onFailure = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    },
                                    place = place
                                )
                            }
                        },
                        modifier = modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        border = BorderStroke(2.dp, Color(0xFF425980)),
                        shape = RoundedCornerShape(13.dp),
                        enabled = isSubmitEnabled.value
                    ) {
                        Text(
                            text = "Submit poll",
                            color = Color(0xFF425980)
                        )
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
fun ViewPlacePreview() {
    isPreviewMode = true
    PicPlaceTheme {
        ViewPlaceScreen(
            modifier = Modifier,
            place = PlaceFirebase(
                userId = "mockUserId",
                imageUrls = listOf("https://example.com/image.jpg"),
                name = "Mock Place",
                description = "This is a mock place.",
                createdAt = System.currentTimeMillis(),
                comments = listOf(),
                likes = 10,
                likedBy = listOf()
            ),
            navController = NavController(LocalContext.current),
            userViewModel = MockUserViewModel(),
            placeViewModel = MockPlaceViewModel()
        )
    }
}