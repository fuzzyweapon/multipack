package com.thecryingbeard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PackUI(isVisible: Boolean, greaterThanClickable: () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Column {
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
            val labels = listOf("Pack Name", "Author", "Contributors", "Version", "Description")
            // Measure the maximum width of the labels outside the composable
            val maxLabelWidth = labels.maxOf { label ->
                with(LocalDensity.current) {
                    // Convert text to DP based on the TextStyle
                    TextStyle(fontSize = 13.sp).fontSize.toPx() * label.length * 0.6f
                }
            } / 2
            PackField("Pack Name", maxLabelWidth.dp, "Neverland", true)
            PackField("Author", maxLabelWidth.dp,"Julia Willson", true)
            PackField("Contributors", maxLabelWidth.dp, "", false)
            PackField("Version", maxLabelWidth.dp, "1.0.0", true)
            PackParagraphField("Description", maxLabelWidth.dp, """This is a sample pack.  Check it out!
                
You can have multiple lines.""", false)
        }
        Divider(
            color = Color.Gray,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
    }
}

@Composable
private fun PackParagraphField(label: String, labelWidth: Dp, text: String, singleLine: Boolean) {
    var value by remember { mutableStateOf(text) }

    val customSelectionColors = TextSelectionColors(
        handleColor = LocalTextSelectionColors.current.handleColor, // Keep the default handle color
        backgroundColor = Color.Gray // Custom highlight color for selected text
    )

    Column(modifier = Modifier.padding(8.5.dp)) {
        Box(modifier = Modifier
            .border(0.3.dp, color = Color.LightGray)
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .border(0.4.dp, color = Color.Gray)
                    .background(Color.White)
                    .padding(8.dp)
                    .width(labelWidth),
                style = TextStyle(color = Color.Black, fontSize = 13.sp)
            )
        }
        Box(
            modifier = Modifier
                .background(Color.LightGray)
        ) {
            CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                BasicTextField(
                    value = value,
                    onValueChange = { it: String -> value = it },
                    modifier = Modifier.background(Color.LightGray).wrapContentSize().padding(8.dp),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp
                    ),
                    singleLine = singleLine,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    cursorBrush = SolidColor(Color.White)
                )
            }
        }
    }
}

@Composable
private fun PackField(label: String, labelWidth: Dp, text: String, singleLine: Boolean) {
    var value by remember { mutableStateOf(text) }

    val customSelectionColors = TextSelectionColors(
        handleColor = LocalTextSelectionColors.current.handleColor, // Keep the default handle color
        backgroundColor = Color.Gray // Custom highlight color for selected text
    )

    Row() {
        Box(modifier = Modifier
            .padding(8.5.dp)
            .border(0.3.dp, color = Color.LightGray)
            ) {
            Text(
                text = label,
                modifier = Modifier
                    .border(0.4.dp, color = Color.Gray)
                    .background(Color.White)
                    .padding(8.dp)
                    .width(labelWidth),
                style = TextStyle(color = Color.Black, fontSize = 13.sp)
            )
        }
        Box(
            modifier = Modifier
                .padding(8.5.dp)
                .background(Color.LightGray)
        ) {
            CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                BasicTextField(
                    value = value,
                    onValueChange = { it: String -> value = it },
                    modifier = Modifier.background(Color.LightGray).wrapContentSize().padding(8.dp),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp
                    ),
                    singleLine = singleLine,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    cursorBrush = SolidColor(Color.White)
                )
            }
        }
    }
}