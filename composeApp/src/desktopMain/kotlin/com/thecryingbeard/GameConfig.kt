package com.thecryingbeard

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(var modsFolderPath: String)
