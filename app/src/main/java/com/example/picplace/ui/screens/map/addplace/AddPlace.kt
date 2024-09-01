package com.example.picplace.ui.screens.map.addplace

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.picplace.models.place.MockPlaceViewModel
import com.example.picplace.models.place.Place
import com.example.picplace.models.place.PlaceViewModel
import com.example.picplace.models.place.SerializableLatLng
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun AddPlaceScreen(
    modifier: Modifier,
    navController: NavController,
    location: LatLng,
    placeViewModel: PlaceViewModel
) {
    var selectedImageUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )
    var description by remember {
        mutableStateOf("")
    }
    var isDescriptionFocused by remember {
        mutableStateOf(false)
    }
    var isAddPollChecked by remember {
        mutableStateOf(false)
    }
    val defaultPoolQuestions: List<PollObjects> = listOf(
        PollObjects(
            question = "How would you rate this location for photography?",
            options = listOf("Excellent", "Good", "Average", "Poor")
        ),
        PollObjects(
            question = "What time of day is best for photography at this location?",
            options = listOf("Sunrise", "Daytime", "Sunset", "Night")
        ),
        PollObjects(
            question = "What type of photography is this location best suited for?",
            options = listOf("Portrait", "Landscape", "Architecture", "Street", "Nature")
        ),
        PollObjects(
            question = "How crowded is this location usually?",
            options = listOf("Very Crowded", "Moderately Crowded", "Occasionally Crowded", "Rarely Crowded")
        ),
        PollObjects(
            question = "Would you recommend this location to others?",
            options = listOf("Yes", "No")
        ),
        PollObjects(
            question = "Does the weather affect the quality of photos at this location?",
            options = listOf("Yes, significantly", "Yes, but only slightly", "No, not really", "Not at all")
        ),
        PollObjects(
            question = "How easy is it to access this location?",
            options = listOf("Very Easy", "Easy", "Moderate", "Difficult")
        )
    )
    var customPollQuestions by remember {
        mutableStateOf<List<PollObjects>>(emptyList())
    }
    var newQuestion by remember {
        mutableStateOf("")
    }
    var newOptions by remember {
        mutableStateOf("")
    }
    var isNewQuestionFocused by remember {
        mutableStateOf(false)
    }
    var isNewOptionFocused by remember {
        mutableStateOf(false)
    }
    var placeName by remember {
        mutableStateOf("")
    }
    var isPlaceFocused by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add Place",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color(0xff425980)
            )

            CustomTextField(
                value = placeName,
                onValueChange = {
                    placeName = it
                },
                label = "Place name",
                isFocused = isPlaceFocused,
                onFocusChange = {
                    isPlaceFocused = it
                },
                imageVector = Icons.Outlined.Title
            )

            Text(
                text = "Add photo/photos",
                color = Color(0xff425980),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Left
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(10.dp),
                modifier = modifier
                    .height(300.dp)
            ) {
                items(selectedImageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(13.dp)
            ) {
                Text(text = "Add photos")
            }

            Text(
                text = "Add description",
                color = Color(0xff425980),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Left
            )

            CustomTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = "Description",
                isFocused = isDescriptionFocused,
                onFocusChange = {
                    isDescriptionFocused = it
                },
                imageVector = Icons.Outlined.Description
            )

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Do you want to add poll?",
                    color = Color(0xff425980),
                    modifier = modifier
                        .weight(1f),
                    textAlign = TextAlign.Left
                )

                Switch(
                    checked = isAddPollChecked,
                    onCheckedChange = {
                        isAddPollChecked = it
                    }
                )
            }

            if (isAddPollChecked) {
                Text(
                    text = "Default questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                defaultPoolQuestions.forEachIndexed { index, poll ->
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "${index + 1}. Question: ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xff425980)
                            )
                            Text(text = poll.question)
                        }

                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "     Options: ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xff425980)
                            )
                            Text(text = poll.options.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (customPollQuestions.isNotEmpty()) {
                    Text(
                        text = "Custom questions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    customPollQuestions.forEachIndexed { index, poll ->
                        Column(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. Question: ",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xff425980)
                                )
                                Text(text = poll.question)

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        customPollQuestions =
                                            customPollQuestions.toMutableList().apply {
                                                removeAt(index)
                                            }
                                    },
                                    modifier = Modifier.padding(start = 8.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "")
                                }
                            }

                            Row(modifier = modifier.fillMaxWidth()) {
                                Text(
                                    text = "     Options: ",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xff425980)
                                )
                                Text(text = poll.options.joinToString(", "))
                            }
                        }
                    }
                }

                Text(
                    text = "Add your own question",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                CustomTextField(
                    value = newQuestion,
                    onValueChange = {
                        newQuestion = it
                    },
                    label = "Question",
                    isFocused = isNewQuestionFocused,
                    onFocusChange = {
                        isNewQuestionFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.Outlined.QuestionMark,
                    capitalization = KeyboardCapitalization.Words
                )

                CustomTextField(
                    value = newOptions,
                    onValueChange = {
                        newOptions = it
                    },
                    label = "Options (comma separated)",
                    isFocused = isNewOptionFocused,
                    onFocusChange = {
                        isNewOptionFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.AutoMirrored.Outlined.List,
                    capitalization = KeyboardCapitalization.Words
                )

                Button(
                    onClick = {
                        if (newQuestion.isNotEmpty() && newOptions.isNotEmpty()) {
                            customPollQuestions = customPollQuestions + PollObjects(
                                question = newQuestion,
                                options = newOptions.split(",").map { it.trim() }
                            )
                            newQuestion = ""
                            newOptions = ""
                        }
                    },
                    shape = RoundedCornerShape(13.dp),
                    modifier = modifier.padding(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff425980),
                        contentColor = Color.White
                    ),
                    enabled = newQuestion.isNotEmpty() && newOptions.isNotEmpty()
                ) {
                    Text(text = "Add Question")
                }
            } else {
                Spacer(modifier = modifier.weight(1f))
            }

            Button(
                onClick = {
                    placeViewModel.addPlace(
                        Place(
                            name = placeName,
                            description = description,
                            imageUris = selectedImageUris,
                            latLng = SerializableLatLng(location.latitude, location.longitude),
                            poll = if(isAddPollChecked) {
                                defaultPoolQuestions + customPollQuestions
                            } else {
                                emptyList()
                            }
                        ),
                        onSuccess = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.Map.screen)
                        },
                        onFailure = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        }
                    )
                },
                shape = RoundedCornerShape(13.dp),
                enabled = selectedImageUris.isNotEmpty() && description.isNotEmpty() && placeName.isNotEmpty(),
                modifier = modifier
                    .align(Alignment.End)
                    .padding(10.dp)
            ) {
                Text(text = "Add place")
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun AddPlacePreview() {
    PicPlaceTheme {
        AddPlaceScreen(
            modifier = Modifier,
            placeViewModel = MockPlaceViewModel(),
            navController = rememberNavController(),
            location = LatLng(0.0, 0.0)
        )
    }
}

data class PollObjects(
    var question: String = "",
    var options: List<String> = emptyList()
)