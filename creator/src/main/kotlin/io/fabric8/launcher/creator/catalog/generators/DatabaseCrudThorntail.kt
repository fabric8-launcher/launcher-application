package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import java.nio.file.Paths

interface DatabaseCrudThorntailProps : RuntimeThorntailProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeThorntailProps.Data(map), DatabaseCrudThorntailProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudThorntail(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dctprops = DatabaseCrudThorntailProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dctprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dctprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dctprops.secretName,
                        "key" to "password"
                    ),
                    "JAVA_OPTIONS" to """-Dthorntail.datasources.data-sources.MyDS.connection-url=jdbc:${dctprops.databaseType}://${'$'}(DB_HOST)/my_data
                     -Dthorntail.datasources.data-sources.MyDS.user-name=${'$'}(DB_USERNAME)
                     -Dthorntail.datasources.data-sources.MyDS.password=${'$'}(DB_PASSWORD)
                     -Dthorntail.datasources.data-sources.MyDS.driver-name=${dctprops.databaseType}""",
                    "GC_MAX_METASPACE_SIZE" to "150",
                    "KUBERNETES_NAMESPACE" to envOf(
                        "field" to "metadata.namespace"
                    )
                )
            )
            generator(::RuntimeThorntail).apply(resources, pprops, extra)
            copy()
            mergePoms(
                Paths.get("merge/pom.${dctprops.databaseType}.xml"))
            transform("src/**/*.java", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dctprops.subFolderName,
                "src/main/java/io/openshift/booster/database/FruitResource.java"
            )
        )
        return resources
    }
}
