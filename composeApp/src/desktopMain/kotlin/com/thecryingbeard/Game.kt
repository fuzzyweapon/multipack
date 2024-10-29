package com.thecryingbeard

import com.thecryingbeard.components.Item
import java.io.File

data class Game(
    override var name: String,                // Name of the game
    override var file: File,                  // Game library directory
    val config: GameConfig?,
) : Item