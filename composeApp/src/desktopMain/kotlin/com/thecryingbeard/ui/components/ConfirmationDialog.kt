package com.thecryingbeard.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    action: String,
    onConfirm: (Boolean) -> Unit // Callback to return the result
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = {
                // If the dialog is dismissed, consider it a "No"
                println("$action canceled")
                onConfirm(false)
            },
            title = { Text(text = "Confirm Action") },
            text = { Text(text = "Are you sure you want to $action?") },
            confirmButton = {
                Button(
                    onClick = {
                    // User confirmed action
                    println("$action confirmed")
                    onConfirm(true)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Yes", color = Color.DarkGray)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                    // User canceled action
                    println("$action canceled")
                    onConfirm(false)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text("No", color = Color.DarkGray)
                }
            }
        )
    }
}