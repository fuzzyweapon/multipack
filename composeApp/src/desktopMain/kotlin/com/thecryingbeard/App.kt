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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.IOException
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

/**
 * Function to create a new folder
 *
 * @param rootDirectoryPath The path to the root directory where the new folder will be created.
 * @param newFolderName The name of the new folder to be created.
 * @return Boolean indicating whether the folder was successfully created.
 */
fun createNewFolder(rootDirectoryPath: String, newFolderName: String): Boolean {
    val newFolder = File(rootDirectoryPath, newFolderName)
    return try {
        if (newFolder.exists()) {
            println("Folder already exists: ${newFolder.absolutePath}")
            false
        } else {
            newFolder.mkdirs().also {
                if (it) {
                    println("Folder created successfully: ${newFolder.absolutePath}")
                } else {
                    println("Failed to create folder: ${newFolder.absolutePath}")
                }
            }
        }
    } catch (e: IOException) {
        println("An error occurred while creating the folder: ${e.message}")
        false
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
                delay(1000)
                showFolderSelectionDialog()
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
    var getGameName by remember { mutableStateOf(false) }
    var getPackName by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        LaunchedEffect(Unit) {
            menusVisible = true
        }

        if (gamesShowing) {
            FadeInColumn(
                title = "Games",
                isVisible = menusVisible,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.Gray,
                files = games,
                background = { game: File? -> if (AppState.selectedGame == game) Color.Gray else Color.LightGray },
                clickable = { game: File? ->
                    AppState.selectedGame = game
                    // Load files from the selected game
                    coroutineScope.launch {
                        game?.let { loadPacks(it) }
                    }
                },
                add = { getGameName = true },
                selectedItem = AppState.selectedGame
            )
        }

        if (getGameName) {
            AppState.selectedFolder?.let { library ->
                NameInputDialog({ getGameName = false }, { name ->
                    createNewFolder(library, name)
                    runBlocking { loadGames(library) }
                    getGameName = false
                })
            }
        }

        (if (gamesShowing) "Packs" else AppState.selectedGame?.name)?.let {
            FadeInColumn(
                title = it,
                isVisible = menusVisible,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.LightGray,
                files = packs,
                background = { file -> if (AppState.selectedPack == file) Color.LightGray else Color.Gray },
                clickable = { file -> AppState.selectedPack = file },
                add = { getPackName = true },
                selectedItem = AppState.selectedPack
            )
        }

        if (getPackName) {
            if (AppState.selectedFolder != null && AppState.selectedGame != null ) {
                NameInputDialog({ getPackName = false }, { name ->
                    createNewFile(AppState.selectedGame!!.absolutePath, "$name.pack")
                    runBlocking { loadPacks(AppState.selectedGame!!) }
                    getPackName = false
                })
            }
        }
    }
}

fun createNewFile(directory: String, fileName: String): File? {
    val newFile = File(directory, fileName)

    return try {
        if (newFile.createNewFile()) {
            println("File created: ${newFile.absolutePath}")
            newFile
        } else {
            println("File already exists.")
            null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun NameInputDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Enter Name", color = Color.DarkGray) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.DarkGray,
                        focusedLabelColor = Color.Gray,
                        focusedIndicatorColor = Color.DarkGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name.text)
                    onDismiss()
                },
                enabled = name.text.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirm", color = Color.DarkGray)
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)) {
                Text("Cancel", color = Color.DarkGray)
            }
        }
    )
}

@Composable
fun FadeInColumn(
    title: String,
    isVisible: Boolean,
    modifier: Modifier,
    color: Color,
    files: List<File>,
    background: (File?) -> Color,
    clickable: (File?) -> Unit,
    add: () -> Unit,
    selectedItem: File?
) {

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Column(
            modifier = Modifier
                .background(color)
                .padding(8.dp)
        ) {

            Row() {
                Text(title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp).weight(1f))
                Text(
                    text = "+",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable { add() }
                )
            }
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
