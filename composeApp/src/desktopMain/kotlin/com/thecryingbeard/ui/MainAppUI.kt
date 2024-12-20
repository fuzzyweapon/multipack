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
import androidx.compose.material.icons.filled.*
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

/**
 * Composable function that sets up the main user interface of the application.
 * Manages the display of games and packs, and handles various UI interactions.
 *
 * @param viewModel The view model that provides data and logic for the UI.
 */
@Composable
fun MainAppUI(
    viewModel: AppViewModel = remember {
        AppViewModelFactory().create(
            AppViewModel::class,
            extras = CreationExtras.Empty
        )
    },
) {
    var menusVisible by remember { mutableStateOf(AppState.menusVisible) }
    val coroutineScope = rememberCoroutineScope()
    var getGameName by remember { mutableStateOf(false) }
    var getPackName by remember { mutableStateOf(false) }
    var openGameSettings by remember { mutableStateOf(false) }

    Column {
        PackUI(isVisible = !menusVisible, greaterThanClickable = { menusVisible = true })
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


            FadeInColumn(
                title = "Games",
                isVisible = menusVisible,
                modifier = Modifier.weight(1f),
                color = Color.Gray,
                libraryLoader = { item: Item? ->
                    item?.let {
                        runBlocking {
                            loadGames(
                                viewModel,
                                item as Library
                            )
                        }
                    }
                },
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

            FadeInColumn(
                title = "Packs",
                isVisible = menusVisible,
                modifier = Modifier.weight(1f),
                color = Color.LightGray,
                libraryLoader = { game: Item? ->  coroutineScope.launch { game?.let { loadPacks(viewModel, it) } } },
                settingsLoader = { item: Item? -> item?.let { } },
                background = { file -> if (AppState.selectedPack?.file == file) Color.LightGray else Color.Gray },
                clickable = { pack ->
                    AppState.selectedPack = pack as Pack
                    menusVisible = false
                },
                add = { getPackName = true },
                selectedItem = AppState.selectedPack,
                selectedParent = AppState.selectedGame,
                menuItems = viewModel.packs,
                viewModel = viewModel,
            )

            when (true) {
                getGameName -> AppState.library?.let { library ->
                    NameInputDialog({ getGameName = false }, { name ->
                        createNewFolder(library.file.absolutePath, name)
                        viewModel.addNewGame(name)
                        getGameName = false
                    })
                }

                getPackName -> if (AppState.library != null && AppState.selectedGame != null) {
                    NameInputDialog({ getPackName = false }, { name ->
                        createNewFile(AppState.selectedGame!!.file.absolutePath, "$name.pack")
                        coroutineScope.launch { loadPacks(viewModel, AppState.selectedGame!!) }
                        getPackName = false
                    })
                }

                openGameSettings -> GameSettingsDialog(
                    selectedGame = AppState.selectedGame!!,
                    onDismiss = { openGameSettings = false },
                    onConfirm = { gameName, settingsModDirText ->
                        renameFolder(AppState.selectedGame!!.file.absolutePath, gameName)
                        viewModel.updateGameName(AppState.selectedGame!!, gameName)
                        viewModel.updateGameDirectory(
                            AppState.selectedGame!!,
                            AppState.library?.file!!.resolve(gameName)
                        )
                        AppState.selectedGame?.config?.modsFolderPath = settingsModDirText
                        openGameSettings = false
                    },
                )

                else -> Unit
            }

        }
    }
}

/**
 * A composable function that displays a fade-in column with a title, icons,
 * and a list of menu items. Provides functionality to add, remove, and load
 * settings for items within the column.
 *
 * @param title The title of the column.
 * @param isVisible Determines if the column is visible.
 * @param modifier Modifier to be applied to the column.
 * @param color The background color for the column.
 * @param libraryLoader Function to load the library for a given item.
 * @param settingsLoader Function to load the settings for a given item.
 * @param background Function to determine the background color for each item.
 * @param clickable Function to handle item click events.
 * @param add Function to add a new item to the column.
 * @param selectedItem The currently selected item in the column.
 * @param selectedParent The parent item of the currently selected item.
 * @param menuItems The list of items to display in the column.
 * @param viewModel The view model to manage the state of the application.
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
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Column(
            modifier = Modifier
                .background(color)
                .fillMaxSize()
                .padding(8.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp).weight(1f))
                Row(horizontalArrangement = Arrangement.End) {
                    RefreshIcon(selectedItem = selectedParent, loader = { directory -> libraryLoader(directory) })
                    if (title == "Games") SettingsIcon(
                        selectedItem = selectedItem,
                        loader = { game -> settingsLoader(game) })
                    PlusIcon(add = add)
                    MinusIcon { showConfirmDeleteDialog = true }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.wrapContentWidth().fillMaxHeight()) {
                items(menuItems.sortedBy { it.name }) { item ->
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
        contentDescription = "Game Settings",
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
        contentDescription = "Refresh Games",
        modifier = Modifier.size(18.dp).clickable {
            selectedItem?.let { file ->
                runBlocking { loader(file) }
            }
        }
    )
}

@Composable
fun PlusIcon(add: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add Game",
        modifier = Modifier.size(18.dp).clickable { add() }
    )
}

@Composable
fun MinusIcon(minus: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Remove,
        contentDescription = "Remove Game",
        modifier = Modifier.size(18.dp).clickable { minus() }
    )
}

@Composable
fun GreaterThanSymbol(clickable: () -> Unit) {
    Text(
        text = ">",
        style = MaterialTheme.typography.h6,
        modifier = Modifier.clickable { clickable() }
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
                    game = viewModel.games.first { game ->
                        game.name == file.parentFile.name
                    }
                )
            }
        )
    }
}