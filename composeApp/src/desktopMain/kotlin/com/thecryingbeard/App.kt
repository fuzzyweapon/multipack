package com.thecryingbeard

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showMainUI by remember { mutableStateOf(false) }

        if (showMainUI) {
            MainAppUI() // Show the main UI
        } else {
            IntroLogo { showMainUI = true } // Transition to main UI
        }
    }
}

@Composable
fun MainAppUI() {
    Text("Welcome to the Main App!")
}
