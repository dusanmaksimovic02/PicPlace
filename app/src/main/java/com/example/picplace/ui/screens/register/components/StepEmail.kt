package com.example.picplace.ui.screens.register.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun StepEmail(
    onNextStep: () -> Unit,
    onBackStep: () -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier,
    registrationViewModel: RegistrationViewModel,
) {

    var isEmailFocused by remember {
        mutableStateOf(false)
    }
    var supportingText by remember {
        mutableStateOf("")
    }
    var borderColor by remember {
        mutableStateOf(Color(0xFF425980))
    }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomTextField(
            value = registrationViewModel.email.value,
            onValueChange = {
                registrationViewModel.email.value = it
                if (registrationViewModel.email.value.isEmpty()) {
                    registrationViewModel.email.isValid = false
                    borderColor = Color(0xFF425980)
                    borderColor = Color.Red
                    supportingText = "Email can't be empty"
                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(registrationViewModel.email.value).matches()) {
                    authViewModel.checkEmailAvailability(registrationViewModel.email.value) { isAvailable ->
                        if (isAvailable) {
                            registrationViewModel.email.isValid = true
                            borderColor = Color(0xFF425980)
                            supportingText = ""
                        } else {
                            registrationViewModel.email.isValid = false
                            borderColor = Color.Red
                            supportingText = "Account with that email already existing"
                        }
                    }
                } else {
                    borderColor = Color.Red
                    supportingText = "Invalid e-mail format"
                }
            },
            label = "Email",
            isFocused = isEmailFocused,
            onFocusChange = {
                isEmailFocused = it
            },
            type = KeyboardType.Email,
            imageVector = Icons.Outlined.Email,
            borderColor = borderColor,
            supportingText = supportingText,
        )

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
            Spacer(modifier = modifier.width(10.dp))

            Button(
                onClick = onNextStep,
                enabled = registrationViewModel.email.isValid
            ) {
                Text(
                    text = "Next"
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
fun PreviewStepEmail() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        StepEmail(
            onBackStep = {},
            onNextStep = {},
            authViewModel = MockAuthViewModel(),
            modifier = Modifier,
            registrationViewModel = RegistrationViewModel()
        )
    }
}