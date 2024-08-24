package com.example.picplace.ui.screens.profile.edituser

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.registration.RegisterViewModelData
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch

@Composable
fun EditUserScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val userData = userViewModel.userData.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    var newUsername by remember {
        mutableStateOf(userData.value!!.username)
    }
    var isNewUsernameValid by remember {
        mutableStateOf(false)
    }
    var isUsernameFocused by remember {
        mutableStateOf(false)
    }
    var newUsernameSupportingText by remember {
        mutableStateOf("")
    }
    var newUsernameBorderColor by remember {
        mutableStateOf(Color(0xFF425980))
    }
    var newEmail by remember {
        mutableStateOf(userData.value!!.email)
    }
    var isNewEmailValid by remember {
        mutableStateOf(false)
    }
    var isEmailFocused by remember {
        mutableStateOf(false)
    }
    var newEmailSupportingText by remember {
        mutableStateOf("")
    }
    var newEmailBorderColor by remember {
        mutableStateOf(Color(0xFF425980))
    }
    var newName by remember {
        mutableStateOf(userData.value!!.name)
    }
    var isNewNameFocused by remember {
        mutableStateOf(false)
    }
    var newSurname by remember {
        mutableStateOf(userData.value!!.surname)
    }
    var isNewSurnameFocused by remember {
        mutableStateOf(false)
    }
    var newPhoneNumber by remember {
        mutableStateOf(userData.value!!.phoneNumber)
    }
    var isNewPhoneNumberFocused by remember {
        mutableStateOf(false)
    }
    val isUsernameChangedAndValid by remember {
        derivedStateOf {
            newUsername != userData.value!!.username && isNewUsernameValid
        }
    }
    val isEmailChangedAndValid by remember {
        derivedStateOf {
            newEmail != userData.value!!.email && isNewEmailValid
        }
    }
    val isOtherDataChanged by remember {
        derivedStateOf {
            newName != userData.value!!.name ||
                    newSurname != userData.value!!.surname ||
                    newPhoneNumber != userData.value!!.phoneNumber
        }
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screens.Login.screen)
            else -> Unit
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(innerPadding)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Edit data",
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color(0xFF425980),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = modifier.weight(1f))

            Column (
                modifier = modifier
                    .border(2.dp, Color(0xFF425980), RoundedCornerShape(13.dp))
                    .padding(10.dp)
            ){
                CustomTextField(
                    value = newUsername,
                    onValueChange = {
                        newUsername = it
                        if (newUsername == userData.value!!.username) {
                            isNewUsernameValid = true
                            newUsernameBorderColor = Color(0xFF425980)
                            newUsernameSupportingText = ""
                        } else if (newUsername.isEmpty()) {
                            isNewUsernameValid = false
                            newUsernameBorderColor = Color(0xFF425980)
                            newUsernameBorderColor = Color.Red
                            newUsernameSupportingText = "Username can't be empty"
                        } else {
                            coroutineScope.launch {
                                authViewModel.checkUsernameAvailability(newUsername) { isAvailable ->
                                    if (isAvailable) {
                                        isNewUsernameValid = true
                                        newUsernameBorderColor = Color(0xFF425980)
                                        newUsernameSupportingText = ""
                                    } else {
                                        isNewUsernameValid = false
                                        newUsernameBorderColor = Color.Red
                                        newUsernameSupportingText = "Username is already taken"
                                    }
                                }
                            }
                        }
                    },
                    label = "New Username",
                    isFocused = isUsernameFocused,
                    onFocusChange = {
                        isUsernameFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.Outlined.Person,
                    borderColor = newUsernameBorderColor,
                    supportingText = newUsernameSupportingText,
                    modifier = modifier
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            userViewModel.changeUserUsername(
                                newUsername = newUsername,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Username is changed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(
                                        context,
                                        "Error while changing username: $exception",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF425980)
                    ),
                    enabled = isUsernameChangedAndValid
                ) {
                    Text(
                        text = "Change username",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = modifier.height(20.dp))

            Column (
                modifier = modifier
                    .border(2.dp, Color(0xFF425980), RoundedCornerShape(13.dp))
                    .padding(10.dp)
            ) {
                CustomTextField(
                    value = newEmail,
                    onValueChange = {
                        newEmail = it
                        if (newEmail == userData.value!!.email) {
                            isNewEmailValid = true
                            newEmailBorderColor = Color(0xFF425980)
                            newEmailSupportingText = ""
                        } else if (newEmail.isEmpty()) {
                            isNewEmailValid = false
                            newEmailBorderColor = Color(0xFF425980)
                            newEmailBorderColor = Color.Red
                            newEmailSupportingText = "Email can't be empty"
                        } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                                .matches()
                        ) {
                            coroutineScope.launch {
                                authViewModel.checkEmailAvailability(newEmail) { isAvailable ->
                                    if (isAvailable) {
                                        isNewEmailValid = true
                                        newEmailBorderColor = Color(0xFF425980)
                                        newEmailSupportingText = ""
                                    } else {
                                        isNewEmailValid = false
                                        newEmailBorderColor = Color.Red
                                        newEmailSupportingText =
                                            "Account with that email already existing"
                                    }
                                }
                            }
                        } else {
                            newEmailBorderColor = Color.Red
                            newEmailSupportingText = "Invalid e-mail format"
                        }
                    },
                    label = "New Email",
                    isFocused = isEmailFocused,
                    onFocusChange = {
                        isEmailFocused = it
                    },
                    type = KeyboardType.Email,
                    imageVector = Icons.Outlined.Email,
                    borderColor = newEmailBorderColor,
                    supportingText = newEmailSupportingText,
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            userViewModel.changeUserEmail(
                                newEmail = newEmail,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Email is changed, please verify your email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    authViewModel.signOut(userViewModel)
                                },
                                onFailure = { exception ->
                                    Toast.makeText(
                                        context,
                                        "Error while changing email: $exception",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.d("ERROR WHILE CHANGING EMAIL", exception.toString())
                                }
                            )
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF425980)
                    ),
                    enabled = isEmailChangedAndValid
                ) {
                    Text(
                        text = "Change e-mail",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = modifier.height(20.dp))

            Column (
                modifier = modifier
                    .border(2.dp, Color(0xFF425980), RoundedCornerShape(13.dp))
                    .padding(10.dp)
            ) {
                CustomTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                    },
                    label = "New Name",
                    isFocused = isNewNameFocused,
                    onFocusChange = {
                        isNewNameFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.Outlined.Person,
                    capitalization = KeyboardCapitalization.Words,
                )

                CustomTextField(
                    value = newSurname,
                    onValueChange = {
                        newSurname = it
                    },
                    label = "New Surname",
                    isFocused = isNewSurnameFocused,
                    onFocusChange = {
                        isNewSurnameFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.Outlined.Person,
                    capitalization = KeyboardCapitalization.Words,
                )

                CustomTextField(
                    value = newPhoneNumber,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { char -> char.isDigit() }

                        val formatted = if (filtered.length < 11) filtered else newPhoneNumber

                        newPhoneNumber = formatted
                    },
                    label = "New Phone number",
                    isFocused = isNewPhoneNumberFocused,
                    onFocusChange = {
                        isNewPhoneNumberFocused = it
                    },
                    type = KeyboardType.Text,
                    imageVector = Icons.Outlined.Phone
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            userViewModel.changeUserData(
                                name = newName,
                                surname = newSurname,
                                phoneNumber = newPhoneNumber,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Your data is changed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(
                                        context,
                                        "Error while changing your data: $exception",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            )
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF425980)
                    ),
                    enabled = isOtherDataChanged
                ) {
                    Text(
                        text = "Change data",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = modifier.weight(1f))

            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = modifier
                    .padding(10.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF425980)),
            ) {
                Text(text = "Back")
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun ProfilePreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        EditUserScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel()
        )
    }
}