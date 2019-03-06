package io.fabric8.launcher.creator.core.resource

import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.data.objectToString
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.existsFromPath
import io.fabric8.launcher.creator.core.streamFromPath
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

// Returns the resources that were read from the given file
fun readResources(resourcesFile: Path): Resources {
    if (existsFromPath(resourcesFile)) {
        try {
            streamFromPath(resourcesFile).use {
                val map = yamlIo.objectFromStream(it)
                return Resources(map as Properties)
            }
        } catch (ex: Exception) {
            System.err.println("Failed to read resources file ${resourcesFile}: ${ex}")
            throw ex
        }
    } else {
        return Resources()
    }
}

// Writes the given resources to the given file
fun writeResources(resourcesFile: Path, res: Resources) {
    try {
        val str = yamlIo.objectToString(res.json)
        Files.createDirectories(resourcesFile.parent)
        resourcesFile.toFile().writeText(str)
    } catch (ex: Exception) {
        System.err.println("Failed to write resources file ${resourcesFile}: ${ex}")
        throw ex
    }
}
