package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.MavenCoords
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.resource.Resources

interface MavenSetupProps : BaseGeneratorProps {
    val maven: MavenCoords

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), MavenSetupProps {
        override var maven: MavenCoords by _map
    }
}

class MavenSetup(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val msprops = MavenSetupProps.build(props)
        updatePom(msprops.application, msprops.maven.groupId, msprops.maven.artifactId, msprops.maven.version)
        return resources
    }
}
