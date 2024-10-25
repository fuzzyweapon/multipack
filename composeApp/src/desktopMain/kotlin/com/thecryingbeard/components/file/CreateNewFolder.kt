package com.thecryingbeard.components.file

import java.io.File
import java.io.IOException

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
            println("Game (directory) already exists: ${newFolder.absolutePath}")
            false
        } else {
            newFolder.mkdirs().also {
                if (it) {
                    println("Game (directory) created successfully: ${newFolder.absolutePath}")
                } else {
                    println("Failed to create the game (directory): ${newFolder.absolutePath}")
                }
            }
        }
    } catch (e: IOException) {
        println("An error occurred while creating the game (directory): ${e.message}")
        false
    }
}