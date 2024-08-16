package com.example.picplace.ui.screens.forgotpassword

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.ui.theme.PicPlaceTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var identifier by remember {
        mutableStateOf("")
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isFieldFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        Button(
            onClick = {
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Find your account",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF425980)
        )

        Text(
            text = "Enter your email or username",
            color = Color(0xFF425980)
        )

        TextField(
            value = identifier,
            onValueChange = {
                identifier = it
            },
            modifier = modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        2.dp,
                        if (isFieldFocused) Color(0xFF425980) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .onFocusChanged { focusState ->
                    isFieldFocused = focusState.isFocused
                },
            shape = RoundedCornerShape(13.dp),
            label = {
                Text(text = "Email/Username")
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    authViewModel.forgotPassword(
                        identifier = identifier,
                        onSuccess = {
                            Toast.makeText(context, "Reset password is successfully send", Toast.LENGTH_LONG).show()
                        },
                        onFailure = {
                            Toast.makeText(context, "Something went wrong with sending reset password email", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF425980)
            ),
        ) {
            Text(
                text = "Send reset password email",
                color = Color.White,
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun ForgotPasswordPreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        ForgotPasswordScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel()
        )
    }
}