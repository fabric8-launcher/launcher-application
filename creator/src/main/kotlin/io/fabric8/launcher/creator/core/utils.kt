package io.fabric8.launcher.creator.core

import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.nio.file.*

fun join(vararg parts: String?) : String {
    return parts.filter { it != null }.joinToString("/")
}

fun streamFromPath(path: Path): InputStream {
    return if (path.isAbsolute) {
        path.toFile().inputStream()
    } else {
        ::streamFromPath.javaClass.classLoader.getResourceAsStream(path.toString())
            ?: throw FileNotFoundException("Couldn't open stream to '$path'")
    }
}

fun existsFromPath(path: Path): Boolean {
    return if (path.isAbsolute) {
        Files.isRegularFile(path)
    } else {
        val url = ::streamFromPath.javaClass.classLoader.getResource(path.toString())
        url != null
    }
}

fun resolveClassPath(path: Path): Path {
    fun getFS(uri: URI): FileSystem {
        return try {
            FileSystems.getFileSystem(uri)
        } catch (ex: FileSystemNotFoundException) {
            FileSystems.newFileSystem(uri, mapOf("create" to "false"))
        }
    }

    return if (path.isAbsolute || Files.exists(path)) {
        path
    } else {
        val url = ::streamFromPath.javaClass.classLoader.getResource(path.toString())
        if (url != null) {
            if (url.protocol == "jar") {
                val (jarfile, filepath) = url.path.split("!", limit=2)
                val jaruri = URI("jar", jarfile, null)
                getFS(jaruri).getPath(filepath)
            } else {
                File(url.toURI()).toPath()
            }
        } else {
            throw FileNotFoundException("Couldn't open stream to '$path'")
        }
    }
}

fun <T> ((T) -> T).compose(func: (T) -> T): (T) -> T = { t: T -> invoke(func.invoke(t)) }

fun <T> compose(vararg funcs: (T) -> T): (T) -> T {
    return funcs.reversedArray().reduce { acc, cur ->
        cur.compose(acc)
    }
}

fun Sequence<String>.writeLines(out: BufferedWriter) {
    forEach { line ->
        out.write(line)
        out.newLine()
    }
}

fun runCmd(cmd: String, vararg args: String?): String {
    return runCmd(null, cmd, *args)
}

fun runCmd(cwd: Path?, cmd: String, vararg args: String?): String {
    val pb = ProcessBuilder(cmd, *args)
    if (cwd != null) {
        pb.directory(cwd.toFile())
    }
    pb.redirectErrorStream(true)
    val p = pb.start()
    val out = p.inputStream.bufferedReader().use { it.readText() }
    val exitCode = p.waitFor()
    if (exitCode != 0) {
        val cmdTxt = "$cmd ${args.joinToString(" ")}"
        throw Exception("Command '$cmdTxt' failed with error code: $exitCode\nOUT: $out")
    }
    return out
}