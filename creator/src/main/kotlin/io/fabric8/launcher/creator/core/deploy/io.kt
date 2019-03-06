package io.fabric8.launcher.creator.core.deploy

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.fabric8.launcher.creator.core.data.jsonIo
import io.fabric8.launcher.creator.core.data.objectToPath
import io.fabric8.launcher.creator.core.data.objectToString
import io.fabric8.launcher.creator.core.existsFromPath
import io.fabric8.launcher.creator.core.streamFromPath
import java.nio.file.Path


// Returns the name of the deployment file in the given directory
fun deploymentFileName(targetDir: Path): Path {
    return targetDir.resolve("deployment.json")
}

// Returns the name of the resources file in the given directory
fun resourcesFileName(targetDir: Path): Path {
    return targetDir.resolve(".openshiftio").resolve("application.yaml")
}

private fun emptyDeploymentDescriptor(): DeploymentDescriptor {
    return DeploymentDescriptor.build {
        applications = mutableListOf()
    }
}

// Returns a promise that will resolve to the JSON
// contents of the given file or to an empty object
// if the file wasn't found
fun readDeployment(deploymentFile: Path): DeploymentDescriptor {
    if (existsFromPath(deploymentFile)) {
        try {
            streamFromPath(deploymentFile).use {
                val obj = Parser.default().parse(it) as JsonObject
                return DeploymentDescriptor.build(obj)
            }
        } catch (ex: Exception) {
            System.err.println("Failed to read deployment file ${deploymentFile}: ${ex}")
            throw ex
        }
    } else {
        return emptyDeploymentDescriptor()
    }
}

// Returns a promise that will resolve when the given
// deployment was written to the given file
fun writeDeployment(deploymentFile: Path, deployment: DeploymentDescriptor) {
    try {
        jsonIo.objectToPath(deployment, deploymentFile)
    } catch (ex: Exception) {
        System.err.println("Failed to write deployment file ${deploymentFile}: ${ex}")
        throw ex
    }
}
