package io.fabric8.launcher.creator.core.analysis

import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.resource.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

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
    return Files.list(dir).use { f -> f.anyMatch { matcher.matches(dir.relativize(it)) } }
}

fun folderTree(dir: Path): Properties {
    val res = propsOf()
    Files.list(dir).use { f ->
        f.filter { Files.isDirectory(it) && it != dir }
            .map { dir.relativize(it) }
            .filter { !it.fileName.toString().startsWith(".") }
            .forEach {
                res[it.toString()] = folderTree(dir.resolve(it))
            }
    }
    return res
}

fun listFolders(root: Path): List<Path> {
    return Files.walk(root).use { f ->
        f.filter { Files.isDirectory(it) }
            .map { root.relativize(it) }
            .filter { !it.startsWith(".git") }
            .toList()
    }
}
