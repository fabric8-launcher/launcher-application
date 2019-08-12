package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.catalog.GeneratorInfo
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.data.objectFromString
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import io.fabric8.launcher.creator.core.maven.maven
import io.fabric8.launcher.creator.core.nodejs.mergePackageJson as mergePackage
import io.fabric8.launcher.creator.core.resource.Resources
import io.fabric8.launcher.creator.core.template.Transformer
import io.fabric8.launcher.creator.core.template.transform
import io.fabric8.launcher.creator.core.template.transformFiles
import java.nio.file.*
import java.nio.file.attribute.PosixFilePermission

val PATH_FILES: Path = Paths.get("files")
val PATH_RESOURCES: Path = Paths.get("resources")
val PATH_POM: Path = Paths.get("pom.xml")
val PATH_MERGE_POM: Path = Paths.get("merge/pom.xml")
val PATH_PACKAGE: Path = Paths.get("package.json")
val PATH_MERGE_PACKAGE: Path = Paths.get("merge/package.json")
val PATH_NONE: Path = Paths.get("")

interface Generator {
    fun apply(resources: Resources, props: Properties, extra: Properties): Resources

    fun postApply(resources: Resources, props: Properties, deployment: DeploymentDescriptor): Resources {
        return resources
    }
}

class GeneratorContext(val targetDir: Path)

typealias GeneratorConstructor = (module: GeneratorInfo, ctx: GeneratorContext) -> Generator

abstract class BaseGenerator(protected val info: GeneratorInfo, protected val ctx: GeneratorContext) : Generator {
    protected val sourceDir: Path
        get() = Paths.get("META-INF/catalog/" + info.infoDef.module)

    protected val targetDir: Path
        get() = ctx.targetDir

    protected fun generator(geninfo: GeneratorInfo): Generator {
        return geninfo.klazz(geninfo, ctx)
    }

    protected fun <T : Generator> generator(
        info: GeneratorInfo,
        genconst: (GeneratorInfo, GeneratorContext) -> T
    ): T {
        return genconst(info, ctx)
    }

    protected fun name(vararg parts: Any?): String {
        return parts.filterNotNull().joinToString("-")
    }

    protected fun copy(from: Path = PATH_FILES, to: Path? = null) {
        if (existsFromPath(sourceDir.resolve(from))) {
            val from2 = resolveClassPath(sourceDir.resolve(from))
            val to2 = if (to != null) targetDir.resolve(to) else targetDir
            copyFiles(from2, to2)
        }
    }

    protected fun copyVersioned(runtime: Runtime?, to: Path? = null) {
        if (runtime != null) {
            val source = Paths.get("files-${validRuntime(runtime).version}")
            copy(source)
        }
    }

    protected fun filesCopied(from: Path = PATH_FILES, to: Path? = null): Boolean {
        if (existsFromPath(sourceDir.resolve(from))) {
            val from2 = resolveClassPath(sourceDir.resolve(from))
            val to2 = if (to != null) targetDir.resolve(to) else targetDir
            return Files.walk(from2).use {
                it.allMatch {
                    val rel = from2.relativize(it)
                    val target = to2.resolve(rel.toString())
                    Files.exists(target)
                }
            }
        } else {
            return true
        }
    }

    protected fun filesCopiedVersioned(runtime: Runtime?, to: Path? = null): Boolean {
        if (runtime != null) {
            val source = Paths.get("files-${validRuntime(runtime).version}")
            return filesCopied(source, to)
        } else {
            return true
        }
    }

    protected fun move(original: Path, to: Path) {
        val original2 = targetDir.resolve(original)
        val to2 = targetDir.resolve(to)
        Files.move(original2, to2, StandardCopyOption.REPLACE_EXISTING)
    }

    protected fun transform(pattern: String, transformer: Transformer, dir: Path = PATH_NONE) {
        transformFiles(resolveClassPath(targetDir.resolve(dir)), pattern, transformer)
    }

    protected fun transform(patterns: List<String>, transformer: Transformer, dir: Path = PATH_NONE) {
        transformFiles(resolveClassPath(targetDir.resolve(dir)), patterns, transformer)
    }

    protected fun appendFile(targetFile: Path, sourceFile: Path) {
        val txt = streamFromPath(sourceDir.resolve(sourceFile)).use {
            it.bufferedReader().readText()
        }
        targetDir.resolve(targetFile).toFile().appendText(txt)
    }

    protected fun updatePom(
        appName: String,
        groupId: String,
        artifactId: String,
        version: String,
        pomFile: Path = PATH_POM
    ) {
        maven.updateMetadata(targetDir.resolve(pomFile), appName, "Generated Application '$appName'")
        maven.updateGAV(targetDir.resolve(pomFile), groupId, artifactId, version)
    }

    protected fun mergePoms(sourcePom: Path = PATH_MERGE_POM, targetPom: Path = PATH_POM) {
        if (existsFromPath(sourceDir.resolve(sourcePom))) {
            maven.mergePoms(targetDir.resolve(targetPom), sourceDir.resolve(sourcePom))
        }
    }

    protected fun mergeVersionedPoms(runtime: Runtime?, targetPom: Path = PATH_POM) {
        if (runtime != null) {
            val sourcePom = Paths.get("merge-${validRuntime(runtime).version}/pom.xml")
            mergePoms(sourcePom)
        }
    }

    protected fun updateMetadata(
        name: String,
        description: String = "A new application generated by the Red Hat Application Creator",
        pomFile: Path = PATH_POM
    ) {
        maven.updateMetadata(targetDir.resolve(pomFile), name, description)
    }

    protected fun mergePackageJson(source: Path = PATH_MERGE_PACKAGE, target: Path = PATH_PACKAGE) {
        if (existsFromPath(sourceDir.resolve(source))) {
            mergePackage(targetDir.resolve(target), sourceDir.resolve(source))
        }
    }

    protected fun mergeVersionedPackageJson(runtime: Runtime?, target: Path = PATH_PACKAGE) {
        if (runtime != null) {
            val sourceJson = Paths.get("merge-${validRuntime(runtime).version}/package.json")
            mergePackageJson(sourceJson)
        }
    }

    protected fun addResources(resources: Resources, transformer: Transformer, from: Path = PATH_RESOURCES) {
        if (existsFromPath(sourceDir.resolve(from))) {
            tryResolveClassPath(sourceDir.resolve(from))?.let { respath ->
                addResourceFiles(respath, resources, transformer)
            }
        }
    }

    protected fun addVersionedResources(resources: Resources, transformer: Transformer, runtime: Runtime?) {
        if (runtime != null) {
            val source = Paths.get("resources-${validRuntime(runtime).version}")
            addResources(resources, transformer, source)
        }
    }
}

private fun copyFiles(from: Path, to: Path) {
    Files.walk(from).use {
        it.forEach {
            if (!Files.isDirectory(it)) {
                val rel = from.relativize(it)
                val target = to.resolve(rel.toString())
                Files.createDirectories(target.parent)
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

                // This is (unfortunately) necessary because when the source of the files is a JAR on
                // the class path we lose all the file permission attributes. So we read them from a
                // file with file name / attributes pairs and restore the required permissions
                // TODO see if we can somehow automate this as part of the build process for example
                val attr = fileAttrs.get(it)
                if (attr != null) {
                    val perms = Files.getPosixFilePermissions(target)
                    if (attr.executable == true) {
                        perms += setOf(
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_EXECUTE
                        )
                    }
                    Files.setPosixFilePermissions(target, perms)
                }
            }
        }
    }
}

private fun addResourceFiles(from: Path, resources: Resources, transformer: Transformer) {
    Files.walk(from).use { paths ->
        paths
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".yaml") || it.toString().endsWith(".yml") }
            .forEach { path ->
                val text = streamFromPath(path).bufferedReader().readText()
                val exptext = transform(text, transformer)
                val yaml = yamlIo.objectFromString(exptext)
                val res = Resources(yaml.deepClone())
                val resnames = res
                    .items
                    .filter { it.kind != null && it.metadata?.name != null }
                    .map { it.kind!! to it.metadata?.name!! }
                if (!resnames.isEmpty() && resnames.all { resources.item(it.first, it.second) == null }) {
                    resnames.forEach { resources.remove(it.first, it.second) }
                    resources.add(res)
                }
            }
    }
}

interface BaseGeneratorProps : BaseProperties {
    val application: String
    val subFolderName: String?
    val serviceName: String
    val routeName: String?

    companion object {
        @JvmOverloads
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
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
        @JvmOverloads
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), BaseGeneratorExtra {
        override var image: String by _map
        override var service: String by _map
    }
}

interface Attrs : BaseProperties {
    val executable: Boolean?

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), Attrs {
        override var executable: Boolean? by _map
    }
}

private val fileAttrs: Map<Path, Attrs> by lazy {
    val f = yamlIo.arrayFromStream(streamFromPath(Paths.get("META-INF/fileattr.yaml")))
    f.map { resolveClassPath(Paths.get("META-INF", it.get("file") as String)) to Attrs.build(it.get("attr") as Properties) }.toMap()
}
