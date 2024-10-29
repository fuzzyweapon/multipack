package com.thecryingbeard.components.file

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import java.io.File

class YamlSerializationService {

    private val yaml = Yaml.default

    // Serialize a Kotlin object to YAML format and save to a file
    fun <T> serialize(data: T, filePath: String, serializer: KSerializer<T>) {
        val yamlString = yaml.encodeToString(serializer, data)
        File(filePath).writeText(yamlString)
    }

    // Deserialize YAML from a file into a Kotlin object
    fun <T> deserialize(filePath: String, serializer: KSerializer<T>): T? {
        if (File(filePath).exists()) {
            val yamlString = File(filePath).readText()
            return yaml.decodeFromString(serializer, yamlString)
        }
        else
            return null
    }
}