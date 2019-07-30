package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.catalog.generators.AppImagesProps
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.Transformer
import io.fabric8.launcher.creator.core.template.transformers.*
import java.nio.file.Path
import java.nio.file.Paths

interface Generator : CatalogItem

typealias GeneratorConstructor = (module: GeneratorInfo, ctx: CatalogItemContext) -> Generator

interface BaseGeneratorProps : BaseProperties {
    val application: String
    val subFolderName: String?
    val serviceName: String
    val routeName: String?

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), BaseGeneratorProps {
        override var application: String by _map
        override var subFolderName: String? by _map
        override var serviceName: String by _map
        override var routeName: String? by _map
    }
}

interface BaseGeneratorExtra : BaseProperties {
    var image: String
    var service: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), BaseGeneratorExtra {
        override var image: String by _map
        override var service: String by _map
    }
}

abstract class BaseGenerator(protected val info: GeneratorInfo, ctx: CatalogItemContext) : BaseCatalogItem(ctx), Generator {
    override val sourceDir: Path
        get() = Paths.get("META-INF/catalog/generators/" + info.infoDef.module)

    abstract override fun apply(resources: Resources, props: Properties, extra: Properties): Resources
}

interface BaseLanguageProps : BaseGeneratorProps {
    val builderImage: String?
    val env: Environment?
    val runtime: Runtime?

    companion object {
        @JvmOverloads
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), BaseLanguageProps {
        override var builderImage: String? by _map
        override var env: Environment? by _map
        override var runtime: Runtime? by _map

        init {
            ensureObject(::runtime, Runtime::Data)
        }
    }
}

class SimpleConfigGenerator(info: GeneratorInfo, ctx: CatalogItemContext) : BaseGenerator(info, ctx) {
    private val DIR_FILES = Paths.get("files")
    private val DIR_RESOURCES = Paths.get("resources")
    private val FILE_FILES_POM = Paths.get("files/pom.xml")

    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        // Update the properties with any values found in config.props
        val useprops = props.deepClone()
        val newprops = expandProps(info.infoDef.config?.props, props)
        useprops.putAll(newprops)
        // Do the same for config.props.env
        val useenv = useprops.getOrDefault("env", envOf()) as Environment
        val newenv = expandProps(info.infoDef.config?.get("props.env") as Environment? ?: envOf(), useprops)
        useenv.putAll(newenv)
        useprops["env"] = useenv
        // Create new expanded config
        val config = ConfigDef.build(expandProps(info.infoDef.config, useprops) as Properties)

        // Execute any configured or predefined actions
        val blprops = BaseLanguageProps.build(useprops)
        if (config.actionsOnce.isNotEmpty()) {
            runActions(config.actionsOnce, resources, blprops)
        } else {
            // Execute predefined actions for generators
            if (config.image != null && config.base != null) {
                throw IllegalArgumentException("Generators can't have both an 'image' and a 'base' property: ${info.infoDef.module}")
            }
            // Apply the base generator (if any)
            config.base?.let {
                generator(GeneratorInfo.valueOf(it)).apply(resources, blprops, extra)
            }
            if (resources.service(blprops.serviceName) == null || !filesCopied() || !filesCopiedVersioned(blprops.runtime)) {
                if (config.image != null) {
                    // An image was specified so we call the `app-images` generator
                    val biprops = AppImagesProps.build(useprops) {
                        image = config.image!!
                    }
                    generator(GeneratorInfo.`app-images`).apply(resources, biprops, extra)
                }
                if (info.infoDef.module.startsWith("runtime-")) {
                    // For runtime generators apply the base runtime support generator
                    generator(GeneratorInfo.`runtime-base-support`).apply(resources, blprops, extra)
                }
                defaultFileActions(resources, blprops, extra)
                defaultResourceActions(resources, blprops, config, blprops.serviceName)
                // And now perform any additional configured actions that only must be run once
                runActions(config.moreActions, resources, blprops)
                if (config.transformFiles.isNotEmpty()) {
                    // Transform all files using the "cases" transformer
                    transform(config.transformFiles, cases(blprops))
                }
            }
            if (config.image != null) {
                setBuildEnv(resources, useenv, blprops.serviceName)
                setDeploymentEnv(resources, useenv, blprops.serviceName)
            }
        }
        // And now perform any other configured actions that have to be run each time
        runActions(config.actionsAlways, resources, blprops)
        // Update the extras object with any values found in config.extra
        updateExtra(extra, config)
        return resources
    }

    private fun defaultResourceActions(
        resources: Resources,
        props: BaseLanguageProps,
        config: ConfigDef,
        serviceName: String
    ) {
        // Any health checks?
        val readiness = config.readinessProbe
        if (readiness is String) {
            setReadinessPath(resources, readiness, serviceName)
        } else if (readiness is Map<*, *>) {
            setHealthProbe(resources, "readinessProbe", readiness as Properties, serviceName)
        } else if (readiness == null) {
            setDefaultReadiness(resources, serviceName)
        }
        val liveness = config.livenessProbe
        if (liveness is String) {
            setLivenessPath(resources, liveness, serviceName)
        } else if (liveness is Map<*, *>) {
            setHealthProbe(resources, "livenessProbe", liveness as Properties, serviceName)
        } else if (liveness == null) {
            setDefaultLiveness(resources, serviceName)
        }
        // Any resource limits?
        val cpuLimit = config.cpuLimit
        if (cpuLimit != null) {
            setCpuLimit(resources, cpuLimit, serviceName)
        }
        val memoryLimit = config.memoryLimit
        if (memoryLimit != null) {
            setMemoryLimit(resources, memoryLimit, serviceName)
        }
        // See if there are any files the "resources" folder
        addResources(resources, cases(props, "#"))
        // See if there are any files the "resources-RUNTIME_VERSION" folder
        addVersionedResources(resources, cases(props, "#"), props.runtime)
    }

    private fun defaultFileActions(
        resources: Resources,
        props: BaseLanguageProps,
        extra: Properties
    ) {
        // Then copy any files found in the "files" folder
        copy()
        // Then copy any files found in the "files-RUNTIME_VERSION" folder
        copyVersioned(props.runtime)
        // If a pom file was copied from the sources we apply the Maven setup generator
        if (existsFromPath(sourceDir.resolve(FILE_FILES_POM))) {
            generator(GeneratorInfo.`maven-setup`).apply(resources, props, extra)
        }
        // If there is a "merge/pom.xml" merge it with the existing pom.xml
        mergePoms()
        // If we have a "runtime" property let's try to merge version poms
        mergeVersionedPoms(props.runtime)
        // If there is a "merge/package.json" merge it with the existing package.json
        mergePackageJson()
        // If there is a "merge-RUNTIME_VERSION/package.json" merge it with the existing package.json
        mergeVersionedPackageJson(props.runtime)
    }

    private fun runActions(actions: List<ActionDef>, resources: Resources, props: BaseLanguageProps) {
        actions.forEach { runAction(it, resources, props) }
    }

    private fun runAction(action: ActionDef, resources: Resources, props: BaseLanguageProps) {
        when (action.action) {
            "apply" -> runApply(action)
            "copy" -> runCopy(action)
            "move" -> runMove(action)
            "transform" -> runTransform(action, props)
            "mergePoms" -> runMergePoms(action)
            "mergePackageJson" -> runMergePackageJson(action)
            "route" -> runRoute(action, resources, props)
            else -> throw IllegalArgumentException("Unknown action '${action.action}'")
        }
    }

    private fun runApply(action: ActionDef) {
        // TODO implement this
    }

    private fun runCopy(action: ActionDef) {
        val from = Paths.get(action.getString("from", PATH_FILES.toString()))
        val to = if (action.containsKey("to")) Paths.get(action.getRequiredString("to")) else null
        copy(from, to)
    }

    private fun runMove(action: ActionDef) {
        val from = Paths.get(action.getRequired<String>("from"))
        val to = Paths.get(action.getRequired<String>("to"))
        move(from, to)
    }

    private fun runTransform(action: ActionDef, props: BaseLanguageProps) {
        val files = action.getRequired<List<String>>("files")
        val transformer = obtainTransformer(action, props)
        transform(files, transformer)
    }


    private fun obtainTransformer(
        action: Properties,
        props: BaseLanguageProps
    ): Transformer {
        return if (action.containsKey("blocks")) {
            val transprops = action.getRequired<Properties>("blocks")
            val startPattern = transprops.getRequiredString("start")
            val endPattern = transprops.getRequiredString("end")
            blocks(startPattern, endPattern, obtainNestableTransformer(transprops, props))
        } else {
            obtainNestableTransformer(action, props)
        }
    }

    private fun obtainNestableTransformer(
        action: Properties,
        props: BaseLanguageProps
    ): Transformer {
        return if (action.containsKey("insertAfter")) {
            val transprops = action.getRequired<Properties>("insertAfter")
            val pattern = transprops.getRequiredString("pattern")
            val lines = actionLines(transprops)
            insertAfter(pattern, lines)
        } else if (action.containsKey("insertBefore")) {
            val transprops = action.getRequired<Properties>("insertBefore")
            val pattern = transprops.getRequiredString("pattern")
            val lines = actionLines(transprops)
            insertBefore(pattern, lines)
        } else if (action.containsKey("insertAtStart")) {
            val transprops = action.getRequired<Properties>("insertAtStart")
            val lines = actionLines(transprops)
            insertAtStart(lines)
        } else if (action.containsKey("insertAtEnd")) {
            val transprops = action.getRequired<Properties>("insertAtEnd")
            val lines = actionLines(transprops)
            insertAtEnd(lines)
        } else if (action.containsKey("insertAtStartOfList")) {
            val transprops = action.getRequired<Properties>("insertAtStartOfList")
            val lines = actionLines(transprops)
            insertAtStartOfList(lines)
        } else if (action.containsKey("insertAtEndOfList")) {
            val transprops = action.getRequired<Properties>("insertAtEndOfList")
            val lines = actionLines(transprops)
            insertAtEndOfList(lines)
        } else if (action.containsKey("replace")) {
            val transprops = action.getRequired<Properties>("replace")
            val pattern = transprops.getRequiredString("pattern")
            val lines = actionLines(transprops)
            replace(pattern, lines)
        } else if (action.containsKey("cases")) {
            val transprops = action.getRequired<Properties>("cases")
            if (transprops.containsKey("props")) {
                cases(transprops.getRequired("props"))
            } else {
                cases(props)
            }
        } else {
            cases(props)
        }
    }

    private fun actionLines(props: Properties): Iterable<String> {
        if (props.containsKey("text")) {
            return props.getRequiredString("text").lineSequence().asIterable()
        } else if (props.containsKey("lines")) {
            return props.getRequired("lines")
        } else if (props.containsKey("fromFile")) {
            val fileName = props.getRequiredString("fromFile")
            val file = sourceDir.resolve(fileName)
            return streamFromPath(file).reader().readLines()
        } else {
            throw IllegalArgumentException("Missing text source properties, use 'text' or 'fromFile'")
        }
    }

    private fun runMergePoms(action: ActionDef) {
        val from = Paths.get(action.getString("from", PATH_MERGE_POM.toString()))
        val to = Paths.get(action.getString("to", PATH_POM.toString()))
        mergePoms(from, to)
    }

    private fun runMergePackageJson(action: ActionDef) {
        val from = Paths.get(action.getString("from", PATH_MERGE_PACKAGE.toString()))
        val to = Paths.get(action.getString("to", PATH_PACKAGE.toString()))
        mergePackageJson(from, to)
    }

    private fun runRoute(action: ActionDef, resources: Resources, props: BaseLanguageProps) {
        val name = action.getString("name", props.routeName ?: "")
        if (name.isNotBlank()) {
            newRoute(resources, name, props.application, props.serviceName)
        }
    }

    private fun <K,V> expandProps(toExpand: Map<K, V>?, expandWith: Properties): MutableMap<K, V> {
        fun expand(item: Any?): Any? {
            return when (item) {
                is Map<*,*> -> mapObject<K,V>(item as Map<K,V>) { key, value ->
                    key to (expand(value) as V)
                }
                is List<*> -> item.map {
                    expand(it)
                }
                is String -> replaceProps(item, expandWith)
                else -> item
            }
        }

        return if (toExpand != null) {
            expand(toExpand) as MutableMap < K, V>
        } else {
            mutableMapOf()
        }
    }

    private fun updateExtra(extra: Properties, withProps: Properties) {
        withProps.keys.filter { it == "extra" || it.startsWith("extra.") }.forEach {
            if (it == "extra") {
                extra.putAll(withProps.getRequired(it))
            } else {
                val key = it.substring(6)
                val fps = extra.pathGet(key, propsOf())
                fps.putAll(withProps.getRequired(it))
                extra.pathPut(key, fps)
            }
        }
        // HACK Just to get the same result we had before without creating too many complexities
        extra.pathGet<Any?>("shared.runtimeInfo.enumInfo")?.let {
            if (it is String) {
                extra.pathPut("shared.runtimeInfo.enumInfo", enumItemNN("runtime.name", it))
            }
        }
    }
}
