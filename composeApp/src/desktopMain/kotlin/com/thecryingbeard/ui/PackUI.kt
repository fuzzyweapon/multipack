package com.thecryingbeard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PackUI(isVisible: Boolean, greaterThanClickable: () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Row(modifier = Modifier.background(Color.Gray).wrapContentSize().fillMaxWidth().padding(8.dp)) {
            val padding = 4.dp
            AppState.selectedGame?.name?.let { selectedGameName ->
                GreaterThanSymbol(clickable = { greaterThanClickable() })
                Text(
                    text = selectedGameName,
                    modifier = Modifier.padding(horizontal = padding),
                    style = MaterialTheme.typography.h6
                )
                Text("/", style = MaterialTheme.typography.h6)
                AppState.selectedPack?.name?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = padding),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}