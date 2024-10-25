package com.thecryingbeard.components.file

import java.io.File
import java.io.IOException

/**
 * Creates a new file in the specified directory.
 *
 * @param directory The path to the directory where the new file will be created.
 * @param fileName The name of the new file to be created.
 * @return The newly created File object, or null if the file already exists or an error occurred.
 */
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