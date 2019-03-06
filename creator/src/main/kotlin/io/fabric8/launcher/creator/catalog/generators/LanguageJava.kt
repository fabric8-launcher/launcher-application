package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface LanguageJavaProps : BaseGeneratorProps {
    val builderImage: String?
    val jarName: String?
    val binaryExt: String?
    val env: Environment?
    val buildArgs: String?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), LanguageJavaProps {
        override var builderImage: String? by _map
        override var jarName: String? by _map
        override var binaryExt: String? by _map
        override var env: Environment? by _map
        override var buildArgs: String? by _map
    }
}

class LanguageJava(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val ljprops = LanguageJavaProps.build(props)
        // Check if the service already exists, so we don't create it twice
        if (resources.service(ljprops.serviceName) == null) {
            copy()
            transform("gap", cases(ljprops))
            val res = newApp(
                    ljprops.serviceName,
                    ljprops.application,
                    ljprops.builderImage ?: BUILDER_JAVA,
                    null,
                    ljprops.env)
            setBuildContextDir(res, ljprops.subFolderName)
            resources.add(res)
            return newRoute(resources, ljprops.routeName, ljprops.application, ljprops.serviceName)
        } else {
            setBuildEnv(resources, ljprops.env, ljprops.serviceName)
            setDeploymentEnv(resources, ljprops.env, ljprops.serviceName)
            return resources
        }
    }
}
