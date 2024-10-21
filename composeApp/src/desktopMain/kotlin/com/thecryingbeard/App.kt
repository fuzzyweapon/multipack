package com.thecryingbeard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

object AppState {
    var menusVisible: Boolean by mutableStateOf(false)
    var selectedFolder: String? by mutableStateOf(null)
    var games: List<File> by mutableStateOf(emptyList())
    var packs: List<File> by mutableStateOf(emptyList())
    var selectedGame: File? by mutableStateOf(null)
    var selectedPack: File? by mutableStateOf(null)
    var gamesShowing: Boolean by mutableStateOf(true)
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

fun showFolderSelectionDialog() {
    SwingUtilities.invokeLater {
        // Start a coroutine to handle the file chooser
        CoroutineScope(Dispatchers.Main).launch {
            AppState.selectedFolder = showFolderDialog()
            if (AppState.selectedFolder != null) {
                println("Selected folder: ${AppState.selectedFolder}")
                loadGames(AppState.selectedFolder!!)
            } else {
                println("Folder selection was canceled.")
            }
        }
    }
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
            }
        }
    }
}

@Composable
fun MainAppUI() {
    val games by remember { derivedStateOf { AppState.games } }
    val packs by remember { derivedStateOf { AppState.packs } }
    val gamesShowing by remember { derivedStateOf { AppState.gamesShowing } }
    var menusVisible by remember { mutableStateOf(AppState.menusVisible) }
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(Unit) {
            menusVisible = true
        }

        if (gamesShowing) {
            FadeInColumn(
                title = "Games",
                isVisible = menusVisible,
                color = Color.Gray,
                files = games, modifier = Modifier.weight(1f).fillMaxHeight(),
                background = { game -> if (AppState.selectedGame == game) Color.Gray else Color.LightGray },
                clickable = { game ->
                    AppState.selectedGame = game
                    // Load files from the selected game
                    coroutineScope.launch {
                        game?.let { loadPacks(it) }
                    }
                },
                selectedItem = AppState.selectedGame
            )
        }

        (if (gamesShowing) "Packs" else AppState.selectedGame?.name)?.let {
            FadeInColumn(
                title = it,
                isVisible = menusVisible,
                color = Color.LightGray,
                files = packs, modifier = Modifier.weight(1f).fillMaxSize(),
                background = { pack -> if (AppState.selectedPack == pack) Color.LightGray else Color.Gray },
                clickable = { pack ->
                    AppState.selectedPack = pack
                },
                selectedItem = AppState.selectedPack
            )
        }


    }
}

@Composable
fun FadeInColumn(
    title: String,
    isVisible: Boolean,
    color: Color,
    files: List<File>,
    modifier: Modifier = Modifier,
    background: (File?) -> Color,
    clickable: (File?) -> Unit,
    selectedItem: File?
) {

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Column(
            modifier = modifier
                .background(color)
                .padding(8.dp)
        ) {

            Text(title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(files) { file ->
                    Text(
                        file.name,
                        modifier = Modifier
                            .background(background(file)) // Set background color
                            .clickable {
                                clickable(file)
                            }
                            .padding(8.dp),
                        style = if (selectedItem == file) {
                            MaterialTheme.typography.body1.copy(textDecoration = TextDecoration.Underline)
                        } else {
                            MaterialTheme.typography.body1
                        }
                    )
                }
            }
        }
    }
}
