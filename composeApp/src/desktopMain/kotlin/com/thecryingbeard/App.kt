package com.thecryingbeard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import multipack.composeapp.generated.resources.Res
import multipack.composeapp.generated.resources.crying_beard_logo_bordered

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            LaunchedEffect(Unit) {
                showContent = true
                delay(2000) // Keep the logo visible for 2 seconds
                showContent = false
            }

            AnimatedVisibility(
                visible = showContent,
                enter = androidx.compose.animation.fadeIn(animationSpec = tween(1000)),
                exit = androidx.compose.animation.fadeOut(animationSpec = tween(3000))
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.crying_beard_logo_bordered), contentDescription = "Logo")
                }
            }
        }
    }
}