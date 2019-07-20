package io.fabric8.launcher.creator.core

import io.fabric8.launcher.creator.core.catalog.enumById
import io.fabric8.launcher.creator.core.data.jsonIo
import io.fabric8.launcher.creator.core.data.objectToString
import kotlin.reflect.KMutableProperty0

fun Map<String, Any?>.pathExists(path: String): Boolean {
    val parts = path.split('.')
    val parents = parts.subList(0, parts.size - 1)
    val parent = parents.fold(this as Map<String, Any>?) { acc, s -> acc?.get(s) as Map<String, Any>? }
    return parent != null && parent.containsKey(parts.last())
}

inline fun <reified T> Map<String, Any?>.pathGet(path: String): T? {
    val res = pathGetInternal(path)
    return if (res is T) res else null
}

inline fun <reified T> Map<String, Any?>.pathGet(path: String, default: T): T {
    val res = pathGetInternal(path)
    return if (res is T) res else default
}

inline fun <reified T> Map<String, Any?>.pathGetRequired(path: String): T {
    val res = pathGetInternal(path)
    return if (res is T) res else throw IllegalArgumentException("Missing required item '$path'")
}

private val indexRe = """([-\w]+)(\[(\d+)\])?""".toRegex()

private fun Map<String, Any?>.pathElementGet(key: String): Any? {
    val m = indexRe.matchEntire(key)
    return if (m != null && m.groups[3] != null) {
        val name = m.groupValues[1]
        val idx = m.groupValues[3].toInt()
        val list = this[name] as? List<Any?>
        if (list == null) {
            null
        } else {
            list[idx]
        }
    } else {
        this[key]
    }
}

private fun MutableMap<String, Any?>.pathElementSet(key: String, value: Any?) {
    val m = indexRe.matchEntire(key)
    return if (m != null && m.groups[3] != null) {
        val name = m.groupValues[0]
        val idx = m.groupValues[3].toInt()
        val list = this[name] as? List<Any?>
        val l = mutableListOf<Any?>()
        if (list != null) {
            l.addAll(list)
        }
        l.set(idx, value)
        this[name] = l
    } else {
        this[key] = value
    }
}

fun Map<String, Any?>.pathGetInternal(path: String): Any? {
    val parts = path.split('.')
    val parents = parts.subList(0, parts.size - 1)
    val parent = parents.fold(this as Map<String, Any?>?) { acc, s -> acc?.pathElementGet(s) as? Map<String, Any?>? }
    return parent?.pathElementGet(parts.last())
}

fun MutableMap<String, Any?>.pathPut(path: String, value: Any?): MutableMap<String, Any?> {
    val parts = path.split('.')
    val parents = parts.subList(0, parts.size - 1)
    val parent = parents.fold(this) { acc, s ->
        var p = acc.pathElementGet(s) as? MutableMap<String, Any?>?
        if (p == null) {
            p = mutableMapOf()
            acc[s] = p
        }
        p
    }
    parent.pathElementSet(parts.last(), value)
    return this
}

inline fun <reified T> Map<String, Any?>.getDefaulted(key: String, default: T): T {
    val res = this[key]
    return if (res is T) res else throw IllegalArgumentException("Missing required item '$key'")
}

inline fun <reified T> Map<String, Any?>.getRequired(key: String): T {
    val res = this[key]
    return if (res is T) res else throw IllegalArgumentException("Missing required item '$key'")
}

inline fun Map<String, Any?>.getString(key: String): String? {
    val res = this[key]
    return if (res != null && res !is Map<*,*> && res !is List<*>) res.toString() else null
}

inline fun Map<String, Any?>.getString(key: String, default: String): String {
    val res = this[key]
    return if (res != null && res !is Map<*, *> && res !is List<*>) res.toString() else default
}

inline fun Map<String, Any?>.getRequiredString(key: String): String {
    val res = this[key]
    return if (res != null && res !is Map<*, *> && res !is List<*>) res.toString() else throw IllegalArgumentException("Missing required item '$key'")
}

fun <K,V> Map<K, V>.deepClone(): MutableMap<K, V> {
    fun clone(item: Any?): Any? {
        return when (item) {
            is Map<*, *> -> mapObject<K, V>(item as Map<K, V>) { key, value ->
                key to (clone(value) as V)
            }
            is List<*> -> item.map {
                clone(it)
            }
            else -> item
        }
    }

    return clone(this) as MutableMap<K, V>
}

// Returns an object with only those key/value pairs that matched the filter
fun <K, V> filterObject(obj: Map<K, V>?, filter: (K, V) -> Boolean): MutableMap<K, V> {
    if (obj != null) {
        val pairs = obj.entries.map { it.toPair() }.filter { filter(it.first, it.second) }
        return mutableMapOf(*pairs.toTypedArray())
    } else {
        return mutableMapOf()
    }
}

// Returns an object with its key/value pairs mapped
fun <K, V> mapObject(obj: Map<K, V>?, mapper: (K, V) -> Pair<K, V>): MutableMap<K, V> {
    if (obj != null) {
        val pairs = obj.entries.map { mapper(it.key, it.value) }
        return mutableMapOf(*pairs.toTypedArray())
    } else {
        return mutableMapOf()
    }
}

fun Map<String, Any?>.toJsonString(): String {
    return jsonIo.objectToString(this)
}

// Properties

typealias Properties = MutableMap<String, Any?>

fun <T: MutableMap<*, *>> T.nonulls(recursive: Boolean = false): T {
    val nullValueKeys = this.keys.filter { get(it) == null }
    nullValueKeys.forEach { remove(it) }
    if (recursive) {
        entries.forEach {
            val v = it.value
            if (v is MutableMap<*, *>) {
                v.nonulls(true)
            } else if (v is MutableList<*>) {
                v.nonulls(true)
            }
        }
    }
    return this
}

fun <T : MutableList<*>> T.nonulls(recursive: Boolean = false): T {
    removeIf { it == null }
    if (recursive) {
        forEach {
            if (it is MutableMap<*, *>) {
                it.nonulls(true)
            } else if (it is MutableList<*>) {
                it.nonulls(true)
            }
        }
    }
    return this
}

fun propsOf(): Properties = mutableMapOf<String, Any?>().withDefault { null }

fun propsOf(vararg pairs: Pair<String, Any?>): Properties = mutableMapOf(*pairs).withDefault { null }

fun propsOfNN(vararg pairs: Pair<String, Any?>) : Properties {
    val m = mutableMapOf<String, Any?>().withDefault { null }
    m.plusAssign(pairs.filter { it.second != null } as Iterable<Pair<String, Any>>)
    return m
}

fun propsOf(map: Map<String, Any?>?, vararg pairs: Pair<String, Any?>): Properties {
    return mutableMapOf<String, Any?>().run {
        if (map != null) {
            putAll(map)
        }
        putAll(pairs)
        this
    }.withDefault { null }
}

fun propsOf(map1: Map<String, Any?>?, map2: Map<String, Any?>?, vararg pairs: Pair<String, Any?>): Properties {
    return mutableMapOf<String, Any?>().run {
        if (map1 != null) {
            putAll(map1)
        }
        if (map2 != null) {
            putAll(map2)
        }
        putAll(pairs)
        this
    }.withDefault { null }
}

private val varsre = """\$\{([a-zA-Z0-9-.]+)(:([^}]*))?}""".toRegex()

fun replaceProps(ref: String, props: Properties): String {
    return varsre.replace(ref) {
        val def = if (it.groupValues.size == 4) it.groupValues[3] else ""
        props.pathGet(it.groupValues[1], def)
    }
}

interface BaseProperties : Properties {

    companion object {
        inline fun <reified T : Data> build(
            klazz: (map: Properties) -> T,
            _map: Properties = propsOf(),
            block: T.() -> Unit = {}
        ): T {
            val newobj = klazz(_map)
            block.invoke(newobj)
            return newobj
        }
    }

    open class Data(map: Properties = propsOf()) : BaseProperties, Properties by map {
        protected val _map: Properties = map.withDefault { null }

        protected inline fun <reified T> ensureObject(prop: KMutableProperty0<T>, klazz: (Properties) -> T) {
            val obj = this[prop.name]
            if (obj != null || !prop.returnType.isMarkedNullable) {
                prop.set(ensureObject(prop.name, obj, klazz))
            }
        }

        protected inline fun <reified T> ensureList(prop: KMutableProperty0<MutableList<T>>, klazz: (Properties) -> T) {
            val list = this[prop.name]
            if (list != null || !prop.returnType.isMarkedNullable) {
                prop.set(ensureList(prop.name, list, klazz) as MutableList<T>)
            }
        }

        protected inline fun <reified T> ensureList(prop: KMutableProperty0<MutableList<T>>) {
            val list = this[prop.name]
            if (list == null && !prop.returnType.isMarkedNullable) {
                prop.set(mutableListOf())
            }
        }

        override fun toString() = _map.toString()

    }
}

inline fun <reified T> ensureObject(name: String, obj: Any?, klazz: (Properties) -> T): T {
    return when (obj) {
        is T -> obj
        is Map<*, *> -> klazz(obj as Properties)
        null -> throw RuntimeException("No value found for required property '$name'")
        else -> throw RuntimeException("Unexpected type for property '$name'")
    }
}

inline fun <reified T> ensureList(name: String, list: Any?, klazz: (Properties) -> T): List<T> {
    return if (list is List<*>) {
        if (list.isEmpty() || list.all { it is T }) {
            list as MutableList<T>
        } else {
            list.map { ensureObject(name, it, klazz) }.toMutableList()
        }
    } else {
        mutableListOf()
    }
}

// Environment

typealias Environment = MutableMap<String, Any>

fun envOf() = mutableMapOf<String, Any>()

fun envOf(vararg pairs: Pair<String, Any>) = mutableMapOf(*pairs)

fun envOf(map: Map<String, Any>?, vararg pairs: Pair<String, Any>): Environment {
    return mutableMapOf<String, Any>().run {
        if (map != null) {
            putAll(map)
        }
        putAll(pairs)
        this
    }
}

// Enums

interface Enumeration: BaseProperties {
    val id: String
    val name: String
    val description: String?
    val metadata: Properties?

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), Enumeration {
        override var id: String by _map
        override var name: String by _map
        override var description: String? by _map
        override var metadata: Properties? by _map
    }
}

typealias Enums = Map<String, List<Enumeration>>

// Misc

interface Runtime : BaseProperties {
    val name: String
    val version: String?

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), Runtime {
        override var name: String by _map
        override var version: String? by _map
    }
}

fun toRuntime(arg: String?): Runtime? {
    return if (arg != null) {
        val parts = arg.split('/', limit = 2)
        val rt = Runtime.build {
            name = parts[0]
            if(parts.size > 1) version = parts[1]
        }
        rt
    } else {
        null
    }
}

fun validRuntime(runtime: Runtime): Runtime {
    val rt = enumById("runtime.name").find { rt -> rt.id == runtime.name }
    if (rt == null) {
        throw IllegalArgumentException("Unknown runtime '${runtime.name}'")
    }
    val versions = enumById("runtime.version.${runtime.name}")
    if (versions.isNullOrEmpty()) {
        throw IllegalStateException("Missing versions for runtime '${runtime.name}'")
    }
    val v = versions.find { v -> v.id == runtime.version }
    return if (v != null) {
        runtime
    } else {
        Runtime.build {
            name = runtime.name
            version = versions[0].id
        }
    }
}

interface DotnetCoords : BaseProperties {
    val namespace: String
    val version: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), DotnetCoords {
        override var namespace: String by _map
        override var version: String by _map
    }
}

interface MavenCoords : BaseProperties {
    val groupId: String
    val artifactId: String
    val version: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), MavenCoords {
        override var groupId: String by _map
        override var artifactId: String by _map
        override var version: String by _map
    }
}

interface NodejsCoords : BaseProperties {
    val name: String
    val version: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), NodejsCoords {
        override var name: String by _map
        override var version: String by _map
    }
}
