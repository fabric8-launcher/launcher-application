package io.fabric8.launcher.creator.catalog

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.MavenCoords
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.catalog.GeneratorContext
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.resource.Resources

interface MavenSetupProps : BaseGeneratorProps {
    val maven: MavenCoords

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(MavenSetupProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map),
        MavenSetupProps {
        override var maven: MavenCoords by _map

        init {
            ensureObject(::maven, MavenCoords::Data)
        }
    }
}

class MavenSetup(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val msprops = MavenSetupProps.build(props)
        updatePom(msprops.application, msprops.maven.groupId, msprops.maven.artifactId, msprops.maven.version)
        return resources
    }
}
