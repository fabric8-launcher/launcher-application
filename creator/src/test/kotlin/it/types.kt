package it

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.Runtime
import io.fabric8.launcher.creator.core.propsOf
import org.junit.jupiter.api.DynamicNode

interface IntegrationTests {
    fun integrationTests(): Iterable<DynamicNode>
}

typealias CapabilityOptions = Map<String, List<Properties>>

data class CapabilityOpts(val name: String, val opts: Properties = propsOf())

interface Part : BaseProperties {
    val runtime: Runtime?
    val folder: String?
    val capabilities: List<CapabilityOpts>

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map),
        Part {
        override var runtime: Runtime? by _map
        override var folder: String? by _map
        override var capabilities: List<CapabilityOpts> by _map
    }
}

data class Context(var routeHost: String? = null)