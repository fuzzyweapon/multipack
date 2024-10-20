package com.thecryingbeard

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

object AppState {
    var selectedFolder: String? by mutableStateOf(null)
}

fun showFileDialog(): String? {
    val fileDialog = FileDialog(Frame(), "Select a file", FileDialog.LOAD)
    fileDialog.isVisible = true
    return if (fileDialog.file != null) {
        "${fileDialog.directory}${fileDialog.file}"
    } else null
}

suspend fun showFolderDialog(): String? {
    return withContext(context = Dispatchers.IO) {
        var selectedFolder: String? = null
        SwingUtilities.invokeAndWait {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFolder = chooser.selectedFile.absolutePath
            }
        }
        selectedFolder
    }
}

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
    // Display the selected file in the UI if any
    Column {
        Text("Welcome to the Main App!")

        AppState.selectedFolder?.let {
            Text("Selected folder: $it")
        }
    }
}
