package com.example.picplace.ui.screens.register.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.components.PasswordTextField
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun StepPassword(
    onNextStep: () -> Unit,
    onBackStep: () -> Unit,
    modifier: Modifier,
    registrationViewModel: RegistrationViewModel,
) {
    var password1 by remember {
        mutableStateOf<String>(registrationViewModel.password.value)
    }
    var password2 by remember {
        mutableStateOf<String>(registrationViewModel.password.value)
    }
    var isPassword1Valid by remember {
        mutableStateOf<Boolean>(registrationViewModel.password.isValid)
    }
    var isPassword2Valid by remember {
        mutableStateOf<Boolean>(registrationViewModel.password.isValid)
    }
    var isPassword1Focused by remember {
        mutableStateOf<Boolean>(false)
    }
    var isPassword2Focused by remember {
        mutableStateOf<Boolean>(false)
    }
    var supportingText1 by remember {
        mutableStateOf<String>("")
    }
    var borderColor1 by remember {
        mutableStateOf<Color>(Color(0xFF425980))
    }
    var supportingText2 by remember {
        mutableStateOf<String>("")
    }
    var borderColor2 by remember {
        mutableStateOf<Color>(Color(0xFF425980))
    }
    val focusManager = LocalFocusManager.current

    fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}\$")
        return regex.matches(password)
    }

    Column (
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        PasswordTextField(
            value = password1,
            onValueChange = {
                password1 = it
                isPassword1Valid = isValidPassword(it)
                if (!isPassword1Valid) {
                    borderColor1 = Color.Red
                    supportingText1 =  "Password must be at least 6 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character."
                } else {
                    borderColor1 = Color(0xFF425980)
                    supportingText1 = ""
                }
            },
            label = "Password",
            isFocused = isPassword1Focused,
            onFocusChange =  {
                isPassword1Focused = it
            },
            borderColor = borderColor1,
            supportingText = supportingText1
        )

        PasswordTextField(
            value = password2,
            onValueChange = {
                password2 = it
                isPassword2Valid = password2 == password1
                if (!isPassword2Valid) {
                    borderColor2 = Color.Red
                    supportingText2 = "Passwords do not match."
                } else
                {
                    borderColor2 = Color(0xFF425980)
                    supportingText2 = ""
                }
            },
            label = "Confirm Password",
            isFocused = isPassword2Focused,
            onFocusChange =  {
                isPassword2Focused = it
            },
            borderColor = borderColor2,
            supportingText = supportingText2
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
                onClick = {
                    registrationViewModel.password.value = password1
                    registrationViewModel.password.isValid = true
                    onNextStep()
                },
                enabled = isPassword1Valid && isPassword2Valid
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
fun PreviewStepPassword() {
    PicPlaceTheme {
        StepPassword(
            onBackStep = {},
            onNextStep = {},
            modifier = Modifier,
            registrationViewModel = RegistrationViewModel()
        )
    }
}