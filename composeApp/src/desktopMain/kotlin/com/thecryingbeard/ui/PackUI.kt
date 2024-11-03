package com.thecryingbeard.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PackUI(greaterThanClickable: () -> Unit) {
    Row(modifier = Modifier.wrapContentSize().fillMaxWidth().padding(8.dp)) {
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