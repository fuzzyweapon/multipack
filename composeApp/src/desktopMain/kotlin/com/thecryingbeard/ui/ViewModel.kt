package com.thecryingbeard.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.thecryingbeard.Game
import com.thecryingbeard.GameConfig
import com.thecryingbeard.Pack
import java.io.File
import kotlin.reflect.KClass

class AppViewModel : ViewModel() {
    private val _games = mutableStateListOf<Game>()
    val games: List<Game> get() = _games


    private val _packs = mutableStateListOf<Pack>()
    val packs: List<Pack> get() = _packs

    // Keep track of UI-specific state in a Map or another data structure
    val gameNames = mutableStateListOf<MutableState<String>>()
    val gameDirs = mutableStateListOf<MutableState<File>>()
    val packNames = mutableStateListOf<MutableState<String>>()
    val gameModDirectories = mutableStateListOf<MutableState<GameConfig>>()
//    val gameNames = _games.map { game -> mutableStateOf(game.name) }
//    val gameDirs = _games.map { game -> mutableStateOf(game.file) }
//    val packNames = _packs.map { pack -> mutableStateOf(pack.name) }
//    val gameModDirectories = _games.map { game -> mutableStateOf(game.config?.modsFolderPath) }

    // Function to set all games at once
    fun setGames(newGames: List<Game>) {
        _games.clear() // Clear existing games
        _games.addAll(newGames) // Add the new list of games

        // Update UI state for game names, directories, and mod directories
        gameNames.clear()
        gameDirs.clear()
        gameModDirectories.clear()

        newGames.forEach { game ->
            gameNames.add(mutableStateOf(game.name))
            gameDirs.add(mutableStateOf(game.file))
            gameModDirectories.add(mutableStateOf(game.config!!))
        }
    }

    fun setPacks(newPacks: List<Pack>) {
        _packs.clear()
        _packs.addAll(newPacks)

        packNames.clear()

        newPacks.forEach { pack ->
            packNames.add(mutableStateOf(pack.name))
        }

    }

    fun updateGameName(game: Game, newName: String) {
        val index = _games.indexOf(game)
        if (index != -1) {
            val newGame = game.copy(name = newName)
            val newFile = AppState.library!!.file.resolve(newName)
            _games[index] = newGame                    // Update the game data itself
            gameNames[index].value = newName           // Update the UI state
            updateGameDirectory(newGame, newFile)

            AppState.selectedGame?.name = newName
            AppState.selectedGame?.file = newFile
        }
    }
    fun updateGameDirectory(game: Game, newDirectory: File) {
        val index = _games.indexOf(game)
        if (index != -1) {
            _games[index] = game.copy(file = newDirectory)  // Update the game data itself
            gameDirs[index].value = newDirectory
        }
    }

    fun updateGameModDirectory(game: Game, modDir: File) {
        val index = _games.indexOf(game)
        if (index != -1) {
            _games[index] = game.copy(config = GameConfig(modDir.absolutePath))
            gameModDirectories[index].value = GameConfig(modDir.absolutePath)
        }
    }

    fun updatePackName(pack: Pack, newName: String) {
        val index = _packs.indexOf(pack)
        if (index != -1) {
            _packs[index] = pack.copy(name = newName)  // Update the pack data itself
            packNames[index].value = newName           // Update the UI state
        }
    }
}

class AppViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass == AppViewModel::class -> AppViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}