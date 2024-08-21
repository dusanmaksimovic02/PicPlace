package com.example.picplace.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF425980),
    supportingText: String = ""
) {
    val passwordVisible = remember {
        mutableStateOf(false)
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                BorderStroke(
                    2.dp,
                    if (isFocused || borderColor == Color.Red) borderColor else Color.Transparent
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        shape = RoundedCornerShape(13.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Password Icon"
            )
        },
        trailingIcon = {
            if (isFocused) {
                val image = if (passwordVisible.value)
                    Icons.Outlined.Visibility
                else
                    Icons.Outlined.VisibilityOff

                IconButton(onClick = {
                    passwordVisible.value = !passwordVisible.value
                }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible.value) "Hide password" else "Show password"
                    )
                }
            }
        }
    )

    if (supportingText.isNotEmpty()) {
        Text(
            text = "*$supportingText",
            color = borderColor,
            modifier = Modifier.padding(vertical = 4.dp, horizontal =  15.dp)
        )
    }
}