package com.thecryingbeard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import multipack.composeapp.generated.resources.Res
import multipack.composeapp.generated.resources.crying_beard_logo_bordered
import org.jetbrains.compose.resources.painterResource

@Composable
fun IntroLogo(onAnimationEnd: () -> Unit) {
    var showLogo by remember { mutableStateOf(false) }
    var fastForward by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LaunchedEffect(Unit) {
            showLogo = true
            if (!fastForward) {
                delay(2000) // Keep the logo visible for 2 seconds
            }
            showLogo = false
            if (!fastForward) {
                delay(3000)
            }
            onAnimationEnd()
        }

        AnimatedVisibility(
            visible = showLogo,
            enter = androidx.compose.animation.fadeIn(animationSpec = tween(1000)),
            exit = androidx.compose.animation.fadeOut(animationSpec = tween(3000))
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.crying_beard_logo_bordered),
                    contentDescription = "Logo",
                    modifier = Modifier.clickable {
                        fastForward = true
                        showLogo = false
                        onAnimationEnd()
                    }
                )
            }
        }
    }
}