package com.thecryingbeard

import com.thecryingbeard.components.Item
import java.io.File

data class Pack(
    override val name: String,
    override val file: File,
    val game: Game,
) : Item
