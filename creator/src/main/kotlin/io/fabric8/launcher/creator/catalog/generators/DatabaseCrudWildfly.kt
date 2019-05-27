package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface DatabaseCrudWildflyProps : RuntimeWildflyProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeWildflyProps.Data(map), DatabaseCrudWildflyProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudWildfly(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcwprops = DatabaseCrudWildflyProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = if ("mysql" == dcwprops.databaseType) {
                propsOf(
                    props,
                    "env" to dbEnv(dcwprops, 3306)
                )
            } else {
                propsOf(
                    props,
                    "env" to dbEnv(dcwprops, 5432)
                )
            }

            generator(::RuntimeWildfly).apply(resources, pprops, extra)
            copy()
            mergePoms()
            transform("src/**/*.java", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcwprops.subFolderName,
                "src/main/java/io/openshift/booster/database/FruitResource.java"
            )
        )
        return resources
    }

    private fun dbEnv(dcwprops: DatabaseCrudWildflyProps, port: Int): Environment {
        return envOf(
            "MYSQL_DATABASE" to "my_data",
            "MYSQL_SERVICE_HOST" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "uri"
            ),
            "MYSQL_SERVICE_PORT" to port,
            "MYSQL_DATASOURCE" to "MyDS",
            "MYSQL_USER" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "user"
            ),
            "MYSQL_PASSWORD" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "password"
            ),
            "GC_MAX_METASPACE_SIZE" to "150",
            "KUBERNETES_NAMESPACE" to envOf(
                "field" to "metadata.namespace"
            )
        )
    }
}
