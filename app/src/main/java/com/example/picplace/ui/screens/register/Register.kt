package com.example.picplace.ui.screens.register

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.picplace.models.auth.AuthState
import com.example.picplace.models.auth.AuthViewModel
import com.example.picplace.models.auth.MockAuthViewModel
import com.example.picplace.models.registration.RegistrationViewModel
import com.example.picplace.ui.navigation.Screens
import com.example.picplace.ui.theme.PicPlaceTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.picplace.ui.screens.register.components.StepEmail
import com.example.picplace.ui.screens.register.components.StepName
import com.example.picplace.ui.screens.register.components.StepPassword
import com.example.picplace.ui.screens.register.components.StepPhoneNumber
import com.example.picplace.ui.screens.register.components.StepProfilePicture
import com.example.picplace.ui.screens.register.components.StepSurname
import com.example.picplace.ui.screens.register.components.StepUsername

@Composable
fun RegisterScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
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

    var currentStep by remember {
        mutableIntStateOf(1)
    }

    val onNextStep: () -> Unit = {
        currentStep++
    }

    val onBackStep: () -> Unit = {
        currentStep--
    }

    when (currentStep) {
        1 -> StepUsername(
            onNextStep = onNextStep,
            authViewModel = authViewModel,
            navController = navController,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        2 -> StepEmail(
            onNextStep = onNextStep,
            authViewModel = authViewModel,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        3 -> StepPassword(
            onNextStep = onNextStep,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        4 -> StepName(
            onNextStep = onNextStep,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        5 -> StepSurname(
            onNextStep = onNextStep,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        6 -> StepPhoneNumber(
            onNextStep = onNextStep,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel
        )
        7 -> StepProfilePicture(
            authViewModel = authViewModel,
            onBackStep = onBackStep,
            modifier = modifier,
            registrationViewModel = registrationViewModel,
            navController = navController
        )
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