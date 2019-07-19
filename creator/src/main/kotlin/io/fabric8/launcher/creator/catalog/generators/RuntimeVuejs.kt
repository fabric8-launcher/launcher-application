package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface RuntimeVuejsProps : LanguageNodejsProps {
    val runtime: Runtime
    val nodejs: NodejsCoords

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageNodejsProps.Data(map), RuntimeVuejsProps {
        override val runtime: Runtime by _map
        override val nodejs: NodejsCoords by _map
    }
}

class RuntimeVuejs(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pvprops = RuntimeVuejsProps.build(props)
        val newenv = envOf(
                pvprops.env,
                "OUTPUT_DIR" to "dist"
        )
        val lprops = LanguageNodejsProps.build(pvprops) {
                env = newenv
                builderImage = BUILDER_NODEJS_WEB
        }

        // Check if the service already exists, so we don"t create it twice
        if (resources.service(pvprops.serviceName) == null) {
            generator(`runtime-base-support`).apply(resources, pvprops, extra)
            copy()
            transform(listOf("package.json", "public/index.html", "README.md"), cases(pvprops))
        }
        val res = generator(`language-nodejs`).apply(resources, lprops, extra)
        setMemoryLimit(resources, "100Mi", pvprops.serviceName)
        setCpuLimit(resources, "200m", pvprops.serviceName)
        setPathHealthChecks(resources, "/", "/", pvprops.serviceName)

        val exProps = propsOf(
                "image" to BUILDER_NODEJS_WEB,
                "enumInfo" to enumItemNN("runtime.name", "vuejs"),
                "service" to pvprops.serviceName,
                "route" to pvprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return res
    }
}
