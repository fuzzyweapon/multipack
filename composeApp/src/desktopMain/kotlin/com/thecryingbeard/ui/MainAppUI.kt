package com.thecryingbeard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import com.thecryingbeard.Game
import com.thecryingbeard.GameConfig
import com.thecryingbeard.Library
import com.thecryingbeard.Pack
import com.thecryingbeard.components.Item
import com.thecryingbeard.components.file.createNewFile
import com.thecryingbeard.components.file.createNewFolder
import com.thecryingbeard.components.file.renameFolder
import com.thecryingbeard.ui.components.ConfirmationDialog
import kotlinx.coroutines.*
import java.io.File
import javax.swing.SwingUtilities

@Composable
fun MainAppUI(
    viewModel: AppViewModel = remember {
        AppViewModelFactory().create(
            AppViewModel::class,
            extras = CreationExtras.Empty
        )
    },
) {
    val gamesShowing by remember { derivedStateOf { AppState.gamesShowing } }
    var menusVisible by remember { mutableStateOf(AppState.menusVisible) }
    val coroutineScope = rememberCoroutineScope()
    var getGameName by remember { mutableStateOf(false) }
    var getPackName by remember { mutableStateOf(false) }
    var openGameSettings by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        LaunchedEffect(Unit) {
            menusVisible = true
            FolderPreferences.getSelectedFolder()?.let {
                val libraryDirectory = File(it)
                AppState.library = Library(libraryDirectory, libraryDirectory.name)
            }
            val library = AppState.library
            if (library != null) {
                loadGames(viewModel, library)
            } else {
                delay(1000)
                showLibrarySelectionDialog(viewModel)
            }
        }

        LaunchedEffect(AppState.selectedGame?.config?.modsFolderPath) {
            val selectedGame = AppState.selectedGame
            if (selectedGame != null) {
                AppState.yamlService.serialize(
                    data = selectedGame.config!!,
                    filePath = selectedGame.file.resolve("game.yaml").canonicalPath,
                    serializer = GameConfig.serializer()
                )
            }
        }

        if (gamesShowing) {
            FadeInColumn(
                title = "Games",
                isVisible = menusVisible,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.Gray,
                libraryLoader = { item: Item? -> item?.let { runBlocking { loadGames(viewModel, item as Library) } } },
                settingsLoader = { item: Item? -> item?.let { openGameSettings = true } },
                background = { game: Item -> if (AppState.selectedGame?.file == game.file) Color.Gray else Color.LightGray },
                clickable = { game: Item ->
                    AppState.selectedGame = game as Game?
                    // Load files from the selected item
                    coroutineScope.launch { loadPacks(viewModel, AppState.selectedGame!!) }
                },
                add = { getGameName = true },
                selectedItem = AppState.selectedGame,
                selectedParent = AppState.library as Item?,
                menuItems = viewModel.games,
                viewModel = viewModel,
            )
        }

        if (getGameName) {
            AppState.library?.let { library ->
                NameInputDialog({ getGameName = false }, { name ->
                    createNewFolder(library.file.absolutePath, name)
                    viewModel.addNewGame(name)
                    getGameName = false
                })
            }
        }

        (if (gamesShowing) "Packs" else AppState.selectedGame?.name)?.let { title ->
            FadeInColumn(
                title = title,
                isVisible = menusVisible,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.LightGray,
                libraryLoader = { game: Item? -> runBlocking { game?.let { loadPacks(viewModel, it) } } },
                settingsLoader = { item: Item? -> item?.let { } },
                background = { file -> if (AppState.selectedPack?.file == file) Color.LightGray else Color.Gray },
                clickable = { pack -> AppState.selectedPack = pack as Pack },
                add = { getPackName = true },
                selectedItem = AppState.selectedPack,
                selectedParent = AppState.selectedGame,
                menuItems = viewModel.packs,
                viewModel = viewModel,
            )
        }

        if (getPackName) {
            if (AppState.library != null && AppState.selectedGame != null) {
                NameInputDialog({ getPackName = false }, { name ->
                    createNewFile(AppState.selectedGame!!.file.absolutePath, "$name.pack")
                    runBlocking { loadPacks(viewModel, AppState.selectedGame!!) }
                    getPackName = false
                })
            }
        }

        if (openGameSettings) {
            GameSettingsDialog(
                selectedGame = AppState.selectedGame!!,
                onDismiss = { openGameSettings = false },
                onConfirm = { gameName, settingsModDirText ->
                    renameFolder(AppState.selectedGame!!.file.absolutePath, gameName)
                    viewModel.updateGameName(AppState.selectedGame!!, gameName)
                    viewModel.updateGameDirectory(AppState.selectedGame!!, AppState.library?.file!!.resolve(gameName))
//                    runBlocking { loadGames(AppState.library!!) }
                    AppState.selectedGame?.config?.modsFolderPath = settingsModDirText
                    openGameSettings = false
                },
            )
        }
    }
}

/**
 * Displays a column with a fade-in animation that contains a title, icons, and a list of items.
 *
 * @param title The title text to display at the top of the column.
 * @param isVisible Whether the column is visible.
 * @param modifier Modifier to be applied to the column.
 * @param color Background color for the column.
 * @param libraryLoader A lambda function to load the library or perform an action, given a `Named` item.
 * @param settingsLoader A lambda function to load settings, given an `Item`.
 * @param background A lambda function that returns a color for the background based on a file.
 * @param clickable A lambda function to handle item click events, given a `Named` item.
 * @param add A lambda function to handle add item action.
 * @param selectedItem The currently selected item.
 * @param selectedParent The parent of the currently selected item.
 * @param menuItems The list of items to display in the column.
 */
@Composable
fun FadeInColumn(
    title: String,
    isVisible: Boolean,
    modifier: Modifier,
    color: Color,
    libraryLoader: (Item?) -> Unit,
    settingsLoader: (Item?) -> Unit,
    background: (Item) -> Color,
    clickable: (Item) -> Unit,
    add: () -> Unit,
    selectedItem: Item?,
    selectedParent: Item?,
    menuItems: List<Item>,
    viewModel: AppViewModel
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp).weight(1f))
                RefreshIcon(selectedItem = selectedParent, loader = { directory -> libraryLoader(directory) })
                if (title == "Games") SettingsIcon(
                    selectedItem = selectedItem,
                    loader = { game -> settingsLoader(game) })
                Text(
                    text = "+",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable { add() }
                )
                Text(
                    text = "-",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable {
                        showConfirmDeleteDialog = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(menuItems) { item ->
                    Text(
                        item.name,
                        modifier = Modifier
                            .background(background(item)) // Set background color
                            .clickable { clickable(item) }
                            .fillParentMaxWidth()
                            .padding(8.dp),
                        style = if (selectedItem?.file?.canonicalPath == item.file.canonicalPath) {
                            MaterialTheme.typography.body1.copy(textDecoration = TextDecoration.Underline)
                        } else {
                            MaterialTheme.typography.body1
                        }
                    )
                }
            }
        }
    }

    selectedItem?.let { item ->
        ConfirmationDialog(showConfirmDeleteDialog, "delete ${item.name}") { confirmed ->
            if (confirmed) {
                runBlocking { item.file.deleteRecursively() }
                viewModel.removeItem(item)
            }
            showConfirmDeleteDialog = false
        }
    }
}

@Composable
fun SettingsIcon(loader: (Item) -> Unit, selectedItem: Item?) {
    Icon(
        imageVector = Icons.Filled.Settings,
        contentDescription = "Settings",
        modifier = Modifier.size(18.dp).clickable {
            selectedItem?.let { item ->
                loader(item)
            }
        }
    )
}

@Composable
fun RefreshIcon(loader: (Item) -> Unit, selectedItem: Item?) {
    Icon(
        imageVector = Icons.Filled.Refresh, // Or Icons.Filled.Restore
        contentDescription = "Refresh Icon",
        modifier = Modifier.size(18.dp).clickable {
            selectedItem?.let { file ->
                runBlocking { loader(file) }
            }
        }
    )
}

@Composable
fun GameSettingsDialog(selectedGame: Game, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var modDir by remember { mutableStateOf(TextFieldValue(selectedGame.config?.modsFolderPath ?: "")) }
    var gameName by remember { mutableStateOf(TextFieldValue(selectedGame.name)) }
    val customSelectionColors = TextSelectionColors(
        handleColor = LocalTextSelectionColors.current.handleColor, // Keep the default handle color
        backgroundColor = Color.LightGray // Custom highlight color for selected text
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Game Settings")
        },
        text = {
            Column {
                CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                    OutlinedTextField(
                        value = gameName,
                        onValueChange = { gameName = it },
                        label = { Text("game-name") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.DarkGray,
                            focusedLabelColor = Color.Gray,
                            focusedBorderColor = Color.Gray,
                            cursorColor = Color.DarkGray,
                        )
                    )
                }
                Row {
                    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                        OutlinedTextField(
                            value = modDir,
                            modifier = Modifier.weight(1f),
                            onValueChange = { modDir = it },
                            label = { Text("mods-directory") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = Color.DarkGray,
                                focusedLabelColor = Color.Gray,
                                focusedBorderColor = Color.Gray,
                                cursorColor = Color.DarkGray,
                            )
                        )
                    }
                    IconButton(
                        onClick = {
                            SwingUtilities.invokeLater {
                                CoroutineScope(Dispatchers.Main).launch {
                                    showFolderDialog()?.let {
                                        modDir = TextFieldValue(it.absolutePath)
                                        println("modDir pointed to ${it.absolutePath}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                            .wrapContentSize(Alignment.CenterEnd),
                    ) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "More options")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(gameName.text, modDir.text) },
                enabled = gameName.text.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NameInputDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    val customSelectionColors = TextSelectionColors(
        handleColor = LocalTextSelectionColors.current.handleColor, // Keep the default handle color
        backgroundColor = Color.LightGray // Custom highlight color for selected text
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Enter Name") },
        text = {
            Column {
                CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.DarkGray,
                            focusedLabelColor = Color.Gray,
                            focusedIndicatorColor = Color.DarkGray,
                            cursorColor = Color.DarkGray
                        )
                    )
                }
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
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)) {
                Text("Cancel")
            }
        }
    )
}

suspend fun loadGames(viewModel: AppViewModel, library: Item) {
    withContext(Dispatchers.IO) {
        val gameDirs = library.file.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
        val gameConfigFileName = "game.yaml"
        viewModel.setGames(
            gameDirs.map { dir ->
                val gameConfigFile = dir.resolve(gameConfigFileName)
                if (!gameConfigFile.exists()) {
                    AppState.yamlService.serialize(
                        data = GameConfig(""),
                        filePath = gameConfigFile.absolutePath,
                        serializer = GameConfig.serializer()
                    )
                }
                val gameConfig =
                    AppState.yamlService.deserialize(
                        dir.resolve(gameConfigFileName).canonicalPath,
                        GameConfig.serializer()
                    )
                Game(name = dir.name, file = dir, gameConfig)
            }
        )
    }
}

suspend fun loadPacks(viewModel: AppViewModel, game: Item) {
    withContext(Dispatchers.IO) {
        val packFiles = game.file.listFiles { file -> file.isFile && file.extension == "pack" }?.toList() ?: emptyList()
        viewModel.setPacks(
            packFiles.map { file ->
                Pack(
                    name = file.name,
                    file = file,
                    game = viewModel.games.firstOrNull { game ->
                        game.name == file.parentFile.name
                    }!!
                )
            }
        )
    }
}