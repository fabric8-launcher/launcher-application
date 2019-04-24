package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.Enumeration
import io.fabric8.launcher.creator.core.Enums
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.data.objectFromPath
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.ensureList
import java.nio.file.Paths

private val enums: Enums by lazy {
    val props = yamlIo.objectFromPath(Paths.get("io/fabric8/launcher/creator/catalog/enums.yaml")) as Properties
    props.keys.forEach { key ->
        props[key] = ensureList(key, props[key], Enumeration::Data)
    }
    props as Enums
}

fun listEnums(): Enums = enums

fun enum(enumId: String): List<Enumeration> {
    return listEnums()[enumId] ?: listOf();
}

fun enumItem(enumId: String, itemId: String): Enumeration? {
    return enum(enumId).find { e -> e.id == itemId }
}

fun enumNN(enumId: String): List<Enumeration> {
    return listEnums().getValue(enumId)
}

fun enumItemNN(enumId: String, itemId: String): Enumeration {
    val item = enumNN(enumId).find { e -> e.id == itemId }
    if (item == null) {
        throw NoSuchElementException("Item ${itemId} not found in enumeration ${enumId}")
    }
    return item
}
