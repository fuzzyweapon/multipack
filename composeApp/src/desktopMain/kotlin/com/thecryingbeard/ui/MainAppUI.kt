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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.thecryingbeard.Game
import com.thecryingbeard.components.file.createNewFile
import com.thecryingbeard.components.file.createNewFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

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
                files = games.map { game -> game.directory },
                loader = { file: File? -> file?.let { runBlocking { loadGames(file) } } },
                background = { game: File? -> if (AppState.selectedGame == game) Color.Gray else Color.LightGray },
                clickable = { game: File? ->
                    AppState.selectedGame = game
                    // Load files from the selected game
                    coroutineScope.launch {
                        game?.let { loadPacks(it) }
                    }
                },
                add = { getGameName = true },
                selectedItem = AppState.selectedGame,
                selectedParent = AppState.selectedFolder
            )
        }

        if (getGameName) {
            AppState.selectedFolder?.let { library ->
                NameInputDialog({ getGameName = false }, { name ->
                    createNewFolder(library.absolutePath, name)
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
                loader = { file: File? -> file?.let { runBlocking { loadPacks(file) } } },
                background = { file -> if (AppState.selectedPack == file) Color.LightGray else Color.Gray },
                clickable = { file -> AppState.selectedPack = file },
                add = { getPackName = true },
                selectedItem = AppState.selectedPack,
                selectedParent = AppState.selectedGame
            )
        }

        if (getPackName) {
            if (AppState.selectedFolder != null && AppState.selectedGame != null) {
                NameInputDialog({ getPackName = false }, { name ->
                    createNewFile(AppState.selectedGame!!.absolutePath, "$name.pack")
                    runBlocking { loadPacks(AppState.selectedGame!!) }
                    getPackName = false
                })
            }
        }
    }
}

@Composable
fun FadeInColumn(
    title: String,
    isVisible: Boolean,
    modifier: Modifier,
    color: Color,
    files: List<File>,
    loader: (File?) -> Unit,
    background: (File?) -> Color,
    clickable: (File?) -> Unit,
    add: () -> Unit,
    selectedItem: File?,
    selectedParent: File?
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp).weight(1f))
                RecycleIcon(selectedItem = selectedParent, loader = { library -> loader(library) })
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

@Composable
fun RecycleIcon(loader: (File) -> Unit, selectedItem: File?) {
    IconButton(
        onClick = {
            selectedItem?.let { file ->
                runBlocking { loader(file) }
            }
        },
        modifier = Modifier.size(18.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh, // Or Icons.Filled.Restore
            contentDescription = "Recycle/Refresh Icon",
        )
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

suspend fun loadGames(file: File) {
    withContext(Dispatchers.IO) {
        val gameDirs = file.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
        AppState.games = gameDirs.map { dir -> Game(dir, dir.name) }
    }
}

suspend fun loadPacks(gameDirectory: File) {
    withContext(Dispatchers.IO) {
        AppState.packs =
            gameDirectory.listFiles { file -> file.isFile && file.extension == "pack" }?.toList() ?: emptyList()
    }
}
