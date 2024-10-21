package com.thecryingbeard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

object AppState {
    var selectedFolder: String? by mutableStateOf(null)
    var games: List<File> by mutableStateOf(emptyList())
    var packs: List<File> by mutableStateOf(emptyList())
    var selectedGame: File? by mutableStateOf(null)
}

suspend fun loadGames(folderPath: String) {
    withContext(Dispatchers.IO) {
        val folder = File(folderPath)
        AppState.games = folder.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }
}

suspend fun loadPacks(gameDirectory: File) {
    withContext(Dispatchers.IO) {
        AppState.packs = gameDirectory.listFiles { file -> file.isFile && file.extension == "pack" }?.toList() ?: emptyList()
    }
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
                delay(3000) // Wait for a moment after the animation completes
                showMainUI = true // Switch to the main UI
            }
        }
    }
}

@Composable
fun MainAppUI() {
    val selectedFolder = AppState.selectedFolder
    val games = AppState.games
    val packs = AppState.packs

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).padding(8.dp)) {
            Text("Games", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            games.forEach { game ->
                Text(
                    game.name,
                    modifier = Modifier
                        .clickable {
                            AppState.selectedGame = game
                            // Load files from the selected game
                            kotlinx.coroutines.GlobalScope.launch {
                                loadPacks(game)
                            }
                        }
                        .padding(8.dp),
                    style = if (AppState.selectedGame == game) {
                        MaterialTheme.typography.body1.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                    } else {
                        MaterialTheme.typography.body1
                    }
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(8.dp)) {
            Text("Packs", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            packs.forEach { pack ->
                Text(pack.name, modifier = Modifier.padding(8.dp))
            }
        }
    }

}
