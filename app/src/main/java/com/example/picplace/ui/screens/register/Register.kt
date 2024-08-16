package com.example.picplace.ui.screens.register

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picplace.R
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.components.PasswordTextField
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun RegisterScreen(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var surname by remember {
        mutableStateOf("")
    }
    var phoneNumber by remember {
        mutableStateOf("")
    }
    var mail by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isUsernameFocused by remember {
        mutableStateOf(false)
    }
    var isPasswordFocused by remember {
        mutableStateOf(false)
    }
    var isNameFocused by remember {
        mutableStateOf(false)
    }
    var isSurnameFocused by remember {
        mutableStateOf(false)
    }
    var isPhoneNumberFocused by  remember {
        mutableStateOf(false)
    }
    var isMailFocused by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate(Screens.Home.screen)
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .verticalScroll(scrollState)
    ) {

        Spacer(modifier = modifier.weight(0.7f))

        Image(
            painter = painterResource(id = R.mipmap.ic_logoo),
            contentDescription = "App Logo",
            modifier = modifier
                .height(150.dp)
                .width(150.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        CustomTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = "Username",
            isFocused = isUsernameFocused,
            onFocusChange =  {
                isUsernameFocused = it
            },
            type = KeyboardType.Text,
            imageVector = Icons.Outlined.Person
        )

        CustomTextField(
            value = mail,
            onValueChange = {
                mail = it
            },
            label = "E-mail",
            isFocused = isMailFocused,
            onFocusChange =  {
                isMailFocused = it
            },
            type = KeyboardType.Email,
            imageVector = Icons.Outlined.Email
        )

        PasswordTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = "Password",
            isFocused = isPasswordFocused,
            onFocusChange =  {
                isPasswordFocused = it
            }
        )

        CustomTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = "Name",
            isFocused = isNameFocused,
            onFocusChange =  {
                isNameFocused = it
            },
            type = KeyboardType.Text,
            imageVector = Icons.Outlined.Person
        )

        CustomTextField(
            value = surname,
            onValueChange = {
                surname = it
            },
            label = "Surname",
            isFocused = isSurnameFocused,
            onFocusChange =  {
                isSurnameFocused = it
            },
            type = KeyboardType.Text,
            imageVector = Icons.Outlined.Person
        )

        CustomTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
            },
            label = "Phone number",
            isFocused = isPhoneNumberFocused,
            onFocusChange =  {
                isPhoneNumberFocused = it
            },
            type = KeyboardType.Phone,
            imageVector = Icons.Outlined.Phone
        )


        Button(
            onClick = {
                authViewModel.register(
                    username = username,
                    password = password,
                    email = mail,
                    name = name,
                    surname = surname,
                    phoneNumber = phoneNumber,
                    onSuccess = {
                        Toast.makeText(context, "Success register, please verify your email", Toast.LENGTH_LONG).show()
                        username = ""
                        password = ""
                        mail = ""
                        name = ""
                        surname = ""
                        phoneNumber = ""
                    },
                    onFailure = {
                        Toast.makeText(context, "Register failed", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF425980)
            ),
            enabled = authState.value != AuthState.Loading
        ) {
            Text(
                text = "Register",
                color = Color.White,
            )
        }

        Spacer(modifier = modifier.weight(1f))

        OutlinedButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            border = BorderStroke(2.dp, Color(0xFF425980))
        ) {
            Text(
                text = "Already have an account?",
                color = Color(0xFF425980)
            )
        }
    }

}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun RegisterPreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        RegisterScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel()
        )
    }
}