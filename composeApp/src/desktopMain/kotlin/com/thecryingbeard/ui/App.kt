package com.thecryingbeard.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.thecryingbeard.Game
import com.thecryingbeard.Library
import com.thecryingbeard.Pack
import com.thecryingbeard.components.file.YamlSerializationService
import kotlinx.coroutines.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

object AppState {
    var menusVisible: Boolean by mutableStateOf(false)
    var library: Library? by mutableStateOf(null)
    var packs: List<Pack> by mutableStateOf(emptyList())
    var selectedGame: Game? by mutableStateOf(null)
    var selectedPack: Pack? by mutableStateOf(null)
    var gamesShowing: Boolean by mutableStateOf(true)
    val yamlService: YamlSerializationService by lazy { YamlSerializationService() }
}

@DelicateCoroutinesApi
@Composable
@Preview
fun App(viewModel: AppViewModel = remember { AppViewModelFactory().create(AppViewModel::class, extras = CreationExtras.Empty) }) {
    MaterialTheme {
        var showMainUI by remember { mutableStateOf(false) }
        var logoAnimationComplete by remember { mutableStateOf(false) }

        if (showMainUI) {
            MainAppUI(viewModel) // Show the main UI
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

fun showLibrarySelectionDialog(viewModel: AppViewModel) {
    SwingUtilities.invokeLater {
        // Start a coroutine to handle the file chooser
        CoroutineScope(Dispatchers.Main).launch {
            showFolderDialog()?.let { AppState.library = Library(it, it.name) }
            val library = AppState.library
            if (library != null) {
                println("Selected library folder: $library")
                loadGames(viewModel, library)
                println("Persisting library folder to Java Preferences.")
                FolderPreferences.setSelectedFolder(library.file.absolutePath)
            } else {
                println("Library folder selection was canceled.")
            }
        }
    }
}

object FolderPreferences {
    private val preferences = Preferences.userRoot().node("multipack")
    const val key = "libraryDirectory"

    fun getSelectedFolder(): String? {
        return preferences.get(key, null)
    }

    fun setSelectedFolder(folderPath: String?) {
        if (folderPath != null) {
            preferences.put(key, folderPath)
        } else {
            preferences.remove(key)
        }
    }
}