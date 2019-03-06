package io.fabric8.launcher.creator.core.data

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

object yamlIo : DataIo {
    override fun objectFromStream(stream: InputStream, charset: Charset): DataObject {
        return Yaml().load(stream)
    }

    override fun arrayFromStream(stream: InputStream, charset: Charset): DataArray {
        return Yaml().load(stream)
    }

    override fun objectToStream(obj: DataObject, stream: OutputStream, charset: Charset) {
        val str = Yaml().dumpAsMap(obj)
        stream.write(str.toByteArray())
    }

    override fun arrayToStream(array: DataArray, stream: OutputStream, charset: Charset) {
        val str = Yaml().dumpAs(array, Tag.SEQ, DumperOptions.FlowStyle.BLOCK)
        stream.write(str.toByteArray())
    }
}
