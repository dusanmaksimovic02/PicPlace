package com.example.picplace.ui.screens.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picplace.R
import com.example.picplace.ui.theme.PicPlaceTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicPlaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var isUsernameFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {

        Spacer(modifier = Modifier.weight(0.7f))

        Image(
            painter = painterResource(id = R.mipmap.ic_logoo),
            contentDescription = "App Logo",
            modifier = Modifier
                .height(150.dp)
                .width(150.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(
                    BorderStroke(
                        2.dp,
                        if (isUsernameFocused) Color(0xFF425980) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .onFocusChanged { focusState ->
                    isUsernameFocused = focusState.isFocused
                },
            value = username,
            onValueChange = {
                username = it
            },
            label = { Text("username") },
            shape = RoundedCornerShape(13.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(
                    BorderStroke(
                        2.dp,
                        if (isPasswordFocused) Color(0xFF425980) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .onFocusChanged { focusState ->
                    isPasswordFocused = focusState.isFocused
                },
            value = password,
            onValueChange = {
                password = it
            },
            label = { Text("password") },
            shape = RoundedCornerShape(13.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Button(
            onClick = {
                Toast.makeText(
                    context,
                    "username: $username, password: $password",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF425980)
            )
        ) {
            Text(
                text = "Login",
                color = Color.White,
            )
        }

        Text(
            text = "Forgotten Password?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(10.dp),
            color = Color(0xFF425980),
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            border = BorderStroke(2.dp, Color(0xFF425980))
        ) {
            Text(
                text = "Create new account",
                color = Color(0xFF425980)
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    PicPlaceTheme {
        LoginScreen()
    }
}