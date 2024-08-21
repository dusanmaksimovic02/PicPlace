package com.example.picplace.ui.screens.register.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun StepPhoneNumber(
    onNextStep: () -> Unit,
    onBackStep: () -> Unit,
    modifier: Modifier,
    registrationViewModel: RegistrationViewModel,
) {
    var isPhoneNumberFocused by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current


    Column (
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CustomTextField(
            value = registrationViewModel.phoneNumber,
            onValueChange = {newValue ->
                val filtered = newValue.filter { char -> char.isDigit()  }

                val formatted = if(filtered.length < 11) filtered else registrationViewModel.phoneNumber

                registrationViewModel.phoneNumber = formatted
            },
            label = "Phone number",
            isFocused = isPhoneNumberFocused,
            onFocusChange = {
                isPhoneNumberFocused = it
            },
            type = KeyboardType.Text,
            imageVector = Icons.Outlined.Phone
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
                enabled = registrationViewModel.phoneNumber.length == 10
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
fun PreviewStepPhoneNumber() {
    PicPlaceTheme {
        StepPhoneNumber(
            onBackStep = {},
            onNextStep = {},
            modifier = Modifier,
            registrationViewModel = RegistrationViewModel()
        )
    }
}