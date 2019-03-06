package io.fabric8.launcher.creator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import io.fabric8.launcher.creator.core.analysis.determineBuilderImage
import io.fabric8.launcher.creator.core.analysis.determineBuilderImageFromGit
import io.fabric8.launcher.creator.core.data.DataObject
import io.fabric8.launcher.creator.core.data.objectToString
import io.fabric8.launcher.creator.core.data.yamlIo
import java.lang.System.exit
import java.nio.file.Paths

class Analyze : CliktCommand(help = "Analyzes a Git repository and reports which builder image is best used to build and run its code") {
    val repo: String by argument(help = "Repository URL or directory")
    val branch: String? by argument(help = "Repository branch").optional()

    override fun run() {
        val img = if (repo.startsWith("http:") || repo.startsWith("https:") || repo.startsWith("git@")) {
            determineBuilderImageFromGit(repo, branch)
        } else {
            determineBuilderImage(Paths.get(repo))
        }
        if (img != null) {
            println(yamlIo.objectToString(img as DataObject))
        } else {
            println("No matching builder image could be found")
            exit(1)
        }
    }
}
