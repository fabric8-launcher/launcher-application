package io.fabric8.launcher.creator.core.analysis

import io.fabric8.launcher.creator.core.resource.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

fun determineBuilderImage(dir: Path): BuilderImage? {
    if (!Files.exists(dir)) {
        throw IllegalArgumentException("Directory doesn't exist")
    }
    return when {
        Files.exists(dir.resolve(".openshiftio/application.yaml")) -> when {
            Files.exists(dir.resolve("deployment.json")) -> markerCreatorImport
            else -> markerBoosterImport
        }
        Files.exists(dir.resolve("pom.xml")) -> when {
            isJavaee(dir) -> builderById(BUILDER_JAVAEE)
            else -> builderById(BUILDER_JAVA)
        }
        Files.exists(dir.resolve("package.json")) -> builderById(BUILDER_NODEJS_APP)
        isDotnet(dir) -> builderById(BUILDER_DOTNET)
        else -> null
    }
}

fun isJavaee(dir: Path): Boolean {
    val pom = dir.resolve("pom.xml").toFile().readText()
    return pom.contains("<packaging>war</packaging>") && !pom.contains("thorntail")
}

fun isDotnet(dir: Path): Boolean {
    // TODO: support sln files and other project formats (fsproj, vbproj)
    val matcher = FileSystems.getDefault().getPathMatcher("glob:*.csproj")
    return Files.list(dir).anyMatch { matcher.matches(dir.relativize(it)) }
}

fun determineBuilderImageFromGit(gitRepoUrl: String, gitRepoBranch: String? = null): BuilderImage? {
    // Create temp dir
    val td = Files.createTempDirectory("creator")
    try {
        // Shallow-clone the repository
        cloneGitRepo(td, gitRepoUrl, gitRepoBranch)
        // From the code we determine the builder image to use
        return determineBuilderImage(td)
    } finally {
        td.toFile().deleteRecursively()
    }
}
