package io.fabric8.launcher.creator.core.data

import io.fabric8.launcher.creator.core.streamFromPath
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

typealias DataObject = Map<String, Any?>
typealias DataArray = List<DataObject>

interface DataIo {
    fun objectFromStream(stream: InputStream, charset: Charset = Charsets.UTF_8): DataObject
    fun arrayFromStream(stream: InputStream, charset: Charset = Charsets.UTF_8): DataArray
    fun objectToStream(obj: DataObject, stream: OutputStream, charset: Charset = Charsets.UTF_8)
    fun arrayToStream(array: DataArray, stream: OutputStream, charset: Charset = Charsets.UTF_8)
}

fun DataIo.objectFromString(json: String): DataObject {
    return objectFromStream(ByteArrayInputStream(json.toByteArray()))
}

fun DataIo.arrayFromString(json: String): DataArray {
    return arrayFromStream(ByteArrayInputStream(json.toByteArray()))
}

fun DataIo.objectFromPath(file: Path): DataObject {
    streamFromPath(file).use {
        return objectFromStream(it)
    }
}

fun DataIo.arrayFromPath(file: Path): DataArray {
    streamFromPath(file).use {
        return arrayFromStream(it)
    }
}

fun DataIo.objectToString(obj: DataObject): String {
    val baos = ByteArrayOutputStream()
    objectToStream(obj, baos)
    return baos.toString()
}

fun DataIo.arrayToString(array: DataArray): String {
    val baos = ByteArrayOutputStream()
    arrayToStream(array, baos)
    return baos.toString()
}

fun DataIo.objectToPath(obj: DataObject, file: Path) {
    Files.newOutputStream(file).use {
        objectToStream(obj, it)
    }
}

fun DataIo.arrayToPath(array: DataArray, file: Path) {
    Files.newOutputStream(file).use {
        arrayToStream(array, it)
    }
}
