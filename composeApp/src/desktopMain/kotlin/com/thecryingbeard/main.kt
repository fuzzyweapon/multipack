package com.thecryingbeard

import androidx.compose.runtime.remember
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.CreationExtras
import com.thecryingbeard.ui.App
import com.thecryingbeard.ui.AppViewModel
import com.thecryingbeard.ui.AppViewModelFactory
import com.thecryingbeard.ui.showLibrarySelectionDialog
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "multipack",
    ) {
        val viewModel = remember { AppViewModelFactory().create(AppViewModel::class, extras = CreationExtras.Empty) }
        MenuBar {
            Menu("File") {
                Item("Open", onClick = {
                    // Ensure we are on the Event Dispatch Thread
                    showLibrarySelectionDialog(viewModel)
                })
                Item("Exit", onClick = ::exitApplication)
            }
        }
        App(viewModel)
    }
}