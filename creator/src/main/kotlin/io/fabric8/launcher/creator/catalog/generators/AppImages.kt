package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.data.objectFromString
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface AppImagesProps : BaseGeneratorProps {
    val image: String

    companion object {
        @JvmOverloads
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), AppImagesProps {
        override var image: String by _map
    }
}

class AppImages(info: GeneratorInfo, ctx: CatalogItemContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val biprops = AppImagesProps.build(props)
        val tplpath = templatePath(biprops.image)
        val text = streamFromPath(tplpath).bufferedReader().readText()
        val exptext = io.fabric8.launcher.creator.core.template.transform(text, cases(props, "#"))
        val yaml = yamlIo.objectFromString(exptext)
        val res = Resources(yaml.deepClone())
        if (res.isEmpty) {
            throw IllegalArgumentException("Image '${biprops.image}' not found")
        }
        setBuildContextDir(res, biprops.subFolderName)
        resources.add(res)
        if (biprops.routeName != null) {
            newRoute(resources, biprops.routeName!!, biprops.application, biprops.serviceName)
        }
        return resources
    }
}
