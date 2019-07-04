package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface RuntimeNodejsProps : LanguageNodejsProps {
    val runtime: Runtime
    val nodejs: NodejsCoords

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageNodejsProps.Data(map), RuntimeNodejsProps {
        override val runtime: Runtime by _map
        override val nodejs: NodejsCoords by _map
    }
}

class RuntimeNodejs(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pnprops = RuntimeNodejsProps.build(props)
        val lprops = LanguageNodejsProps.build(pnprops) {
            builderImage = BUILDER_NODEJS_APP
        }

        // Check if the service already exists, so we don't create it twice
        if (resources.service(pnprops.serviceName) == null) {
            generator(::RuntimeBaseSupport).apply(resources, pnprops, extra)
            copy()
            transform(listOf("package.json"), cases(pnprops))
        }
        val res = generator(::LanguageNodejs).apply(resources, lprops, extra)
        setMemoryLimit(res, "768Mi")
        setPathHealthChecks(resources, "/", "/", lprops.serviceName)

        val exProps = propsOf(
                "image" to BUILDER_NODEJS_APP,
                "enumInfo" to enumItemNN("runtime.name", "nodejs"),
                "service" to pnprops.serviceName,
                "route" to pnprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return res
    }
}
