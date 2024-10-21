package com.thecryingbeard

import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import javax.swing.SwingUtilities

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "multipack",
    ) {
        MenuBar {
            Menu("File") {
                Item("Open", onClick = {
                    // Ensure we are on the Event Dispatch Thread
                    showFolderSelectionDialog()
                })
                Item("Exit", onClick = ::exitApplication)
            }
        }
        App()
    }
}