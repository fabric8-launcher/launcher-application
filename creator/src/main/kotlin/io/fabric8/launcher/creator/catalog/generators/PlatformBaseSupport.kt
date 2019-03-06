package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import java.nio.file.Paths

interface PlatformBaseSupportProps : BaseGeneratorProps {
    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), PlatformBaseSupportProps {
    }
}

class PlatformBaseSupport(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pbsprops = PlatformBaseSupportProps.build(props)
        // This is here in case we get applied in a subFolderName of our own
        // (meaning there's no runtime so there's no gap or README)
        val files = Paths.get("files")
        val parent = Paths.get("..")
        if (pbsprops.subFolderName != null && !filesCopied(files, parent)) {
            copy(files, parent)
            transform("gap", cases(pbsprops), parent)
        }
        return resources
    }
}