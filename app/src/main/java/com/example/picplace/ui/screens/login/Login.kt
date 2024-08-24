package com.example.picplace.ui.screens.login

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.picplace.models.user.MockUserViewModel
import com.example.picplace.models.user.UserViewModel
import com.example.picplace.ui.theme.PicPlaceTheme
import com.example.picplace.ui.components.CustomTextField
import com.example.picplace.ui.components.PasswordTextField
import com.example.picplace.ui.navigation.Screens
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
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

    val authState = authViewModel.authState.observeAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate(Screens.Home.screen)
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
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
            label = "E-mail/Username",
            isFocused = isUsernameFocused,
            onFocusChange = {
                isUsernameFocused = it
            },
            type = KeyboardType.Email,
            imageVector = Icons.Outlined.Person
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

        Button(
            onClick = {
                coroutineScope.launch{
                    authViewModel.login(
                       username = username,
                        password = password,
                        userViewModel = userViewModel
                    )
                }
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
                text = "Login",
                color = Color.White,
            )
        }

        TextButton(
            onClick = {
                navController.navigate(Screens.ForgotPassword.screen)
                      },
            modifier = modifier
                .align(Alignment.End)
                .padding(horizontal = 10.dp),
        ){
            Text(
                text = "Forgotten Password?",
                color = Color(0xFF425980),
                )
        }

        Spacer(modifier = modifier.weight(1f))

        OutlinedButton(
            onClick = {
                navController.navigate(Screens.Register.screen)
            },
            modifier = modifier
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


@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun LoginPreview() {
    AuthViewModel.isPreviewMode = true
    PicPlaceTheme {
        LoginScreen(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel()
        )
    }
}