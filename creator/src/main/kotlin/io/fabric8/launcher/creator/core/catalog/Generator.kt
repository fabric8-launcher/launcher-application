package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.resource.Resources

interface Generator : CatalogItem {
}

typealias GeneratorConstructor = (ctx: CatalogItemContext) -> Generator

interface BaseGeneratorProps : BaseProperties {
    val application: String
    val subFolderName: String?
    val serviceName: String
    val routeName: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), BaseGeneratorProps {
        override var application: String by _map
        override var subFolderName: String? by _map
        override var serviceName: String by _map
        override var routeName: String by _map
    }
}

interface BaseGeneratorExtra : BaseProperties {
    var image: String
    var service: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), BaseGeneratorExtra {
        override var image: String by _map
        override var service: String by _map
    }
}

abstract class BaseGenerator(ctx: CatalogItemContext) : BaseCatalogItem(ctx), Generator {
    abstract override fun apply(resources: Resources, props: Properties, extra: Properties): Resources
}
