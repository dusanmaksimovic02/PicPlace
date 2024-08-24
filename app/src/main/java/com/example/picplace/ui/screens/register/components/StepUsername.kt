package com.example.picplace.ui.screens.register.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.picplace.R
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch

@Composable
fun StepUsername(
    onNextStep: () -> Unit,
    authViewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier,
    registrationViewModel: RegistrationViewModel,
) {
    var isUsernameFocused by remember {
        mutableStateOf(false)
    }
    var supportingText by remember {
        mutableStateOf("")
    }
    var borderColor by remember {
        mutableStateOf(Color(0xFF425980))
    }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {

        Spacer(modifier = modifier.weight(0.4f))

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
            value = registrationViewModel.username.value,
            onValueChange = {
                registrationViewModel.username.value = it
                if (registrationViewModel.username.value.isEmpty()) {
                    registrationViewModel.username.isValid = false
                    borderColor = Color(0xFF425980)
                    borderColor = Color.Red
                    supportingText = "Username can't be empty"
                } else {
                    coroutineScope.launch {
                        authViewModel.checkUsernameAvailability(registrationViewModel.username.value) { isAvailable ->
                            if (isAvailable) {
                                registrationViewModel.username.isValid = true
                                borderColor = Color(0xFF425980)
                                supportingText = ""
                            } else {
                                registrationViewModel.username.isValid = false
                                borderColor = Color.Red
                                supportingText = "Username is already taken"
                            }
                        }
                    }
                }
            },
            label = "Username",
            isFocused = isUsernameFocused,
            onFocusChange = {
                isUsernameFocused = it
            },
            type = KeyboardType.Text,
            imageVector = Icons.Outlined.Person,
            borderColor = borderColor,
            supportingText = supportingText,
            modifier = modifier
        )

        Button(
            onClick = onNextStep,
            enabled = registrationViewModel.username.isValid,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp)
        ) {
            Text(
                text = "Next"
            )
        }

        Spacer(modifier = modifier.weight(1f))

        OutlinedButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 20.dp),
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
fun PreviewStepUsername() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        StepUsername(
            onNextStep = {},
            authViewModel = MockAuthViewModel(),
            modifier = Modifier,
            navController = rememberNavController(),
            registrationViewModel = RegistrationViewModel()
        )
    }
}