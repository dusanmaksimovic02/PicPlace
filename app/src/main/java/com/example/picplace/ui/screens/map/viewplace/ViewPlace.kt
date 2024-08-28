package com.example.picplace.ui.screens.map.addplace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.picplace.ui.theme.PicPlaceTheme

@Composable
fun ViewPlaceScreen(modifier: Modifier) {
    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 2
)
@Composable
fun ViewPlacePreview() {
    PicPlaceTheme {
        ViewPlaceScreen(
            modifier = Modifier
        )
    }
}