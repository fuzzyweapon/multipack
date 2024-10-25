package com.thecryingbeard.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import com.thecryingbeard.Game
import kotlinx.coroutines.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

object AppState {
    var menusVisible: Boolean by mutableStateOf(false)
    var selectedFolder: File? by mutableStateOf(null)
    var games: List<Game> by mutableStateOf(emptyList())
    var packs: List<File> by mutableStateOf(emptyList())
    var selectedGame: File? by mutableStateOf(null)
    var selectedPack: File? by mutableStateOf(null)
    var gamesShowing: Boolean by mutableStateOf(true)
}

@DelicateCoroutinesApi
@Composable
@Preview
fun App() {
    MaterialTheme {
        var showMainUI by remember { mutableStateOf(false) }
        var logoAnimationComplete by remember { mutableStateOf(false) }

        if (showMainUI) {
            MainAppUI() // Show the main UI
        } else {
            IntroLogo (
                onAnimationEnd = {
                    logoAnimationComplete = true // Animation has completed
                }
            )
        }

        // Use LaunchedEffect to control the timing of the transition to the main UI
        LaunchedEffect(logoAnimationComplete) {
            if (logoAnimationComplete) {
                showMainUI = true // Switch to the main UI
                delay(1000)
                showFolderSelectionDialog()
            }
        }
    }
}

fun showFileDialog(): String? {
    val fileDialog = FileDialog(Frame(), "Select a file", FileDialog.LOAD)
    fileDialog.isVisible = true
    return if (fileDialog.file != null) {
        "${fileDialog.directory}${fileDialog.file}"
    } else null
}

suspend fun showFolderDialog(): File? {
    return withContext(context = Dispatchers.IO) {
        var selectedFolder: File? = null
        SwingUtilities.invokeAndWait {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFolder = chooser.selectedFile
            }
        }
        selectedFolder
    }
}

fun showFolderSelectionDialog() {
    SwingUtilities.invokeLater {
        // Start a coroutine to handle the file chooser
        CoroutineScope(Dispatchers.Main).launch {
            AppState.selectedFolder = showFolderDialog()
            val dir = AppState.selectedFolder
            if (dir != null) {
                println("Selected library folder: $dir")
                loadGames(dir)
            } else {
                println("Library folder selection was canceled.")
            }
        }
    }
}