package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import java.nio.file.Paths

interface DatabaseCrudQuarkusProps : RuntimeQuarkusProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeQuarkusProps.Data(map), DatabaseCrudQuarkusProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudQuarkus(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcqprops = DatabaseCrudQuarkusProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dcqprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dcqprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dcqprops.secretName,
                        "key" to "password"
                    ),
                    "JAVA_OPTIONS" to """-Dquarkus.datasource.url=jdbc:${dcqprops.databaseType}://$(DB_HOST)/my_data
                        -Dquarkus.datasource.username=$(DB_USERNAME)
                        -Dquarkus.datasource.password=$(DB_PASSWORD)""",
                    "GC_MAX_METASPACE_SIZE" to "150",
                    "KUBERNETES_NAMESPACE" to envOf(
                        "field" to "metadata.namespace"
                    )
                )
            )
            generator(`runtime-quarkus`).apply(resources, pprops, extra)
            copy()
            copy(Paths.get("merge/application-local.properties"), Paths.get("src/main/resources/application-local.properties"))
            mergePoms(Paths.get("merge/pom.${dcqprops.databaseType}.xml"))
            appendFile(
                Paths.get("src/main/resources/application.properties"),
                Paths.get("merge/application.${dcqprops.databaseType}.properties")
            )
            transform("src/**/*.java", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcqprops.subFolderName,
                "src/main/java/io/openshift/booster/database/FruitResource.java"
            )
        )
        return resources
    }
}
