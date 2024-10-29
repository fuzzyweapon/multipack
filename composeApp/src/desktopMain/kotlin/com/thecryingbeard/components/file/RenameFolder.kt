package com.thecryingbeard.components.file

import java.io.File

fun renameFolder(currentPath: String, newName: String): Boolean {
    val folder = File(currentPath)
    val newFolder = File(folder.parentFile, newName)
    val renamed = folder.renameTo(newFolder)
    if (renamed)
        println("$currentPath renamed to $newName")
    else
        println("$currentPath NOT renamed to $newName")
    return renamed
}