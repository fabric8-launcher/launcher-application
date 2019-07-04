package io.fabric8.launcher.creator.core.data

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import io.fabric8.launcher.creator.core.toJsonString
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

object jsonIo: DataIo {
    override fun objectFromStream(stream: InputStream, charset: Charset): DataObject {
        return Klaxon().parseJsonObject(stream.reader(charset))
    }

    override fun arrayFromStream(stream: InputStream, charset: Charset): DataArray {
        return Klaxon().parseJsonArray(stream.reader(charset)) as DataArray
    }

    override fun objectToStream(obj: DataObject, stream: OutputStream, charset: Charset) {
        val str = if (obj is JsonBase) {
            obj.toJsonString(true)
        } else {
            // This is so dumb!
            val jobj = objectFromString(Klaxon().toJsonString(obj)) as JsonObject
            jobj.toJsonString(true)
        }
        stream.write(str.toByteArray())
    }

    override fun arrayToStream(array: DataArray, stream: OutputStream, charset: Charset) {
        val str = if (array is JsonBase) {
            array.toJsonString(true)
        } else {
            // This is so dumb!
            val jarray = arrayFromString(Klaxon().toJsonString(array)) as JsonArray<*>
            jarray.toJsonString(true)
        }
        stream.write(str.toByteArray())
    }
}
