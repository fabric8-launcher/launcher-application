package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*

interface DatabaseSecretRef {
    val secretName: String
}

interface DatabaseSecretProps : BaseGeneratorProps, DatabaseSecretRef {
    val databaseName: String
    val databaseUri: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), DatabaseSecretProps {
        override var databaseName: String by _map
        override var databaseUri: String by _map
        override var secretName: String by _map
    }
}

class DatabaseSecret(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dsprops = DatabaseSecretProps.build(props)
        // Check if the service already exists, so we don't create it twice
        if (resources.secret(dsprops.secretName) == null) {
            // Create Secret holding Database connection/authentication information
            val secret = propsOf(
                "kind" to "Secret",
                "apiVersion" to "v1",
                "metadata" to propsOf(
                    "name" to dsprops.secretName,
                    "labels" to propsOf(
                        "app" to dsprops.application
                    )
                ),
                "stringData" to propsOf(
                    "uri" to dsprops.databaseUri,
                    "database" to dsprops.databaseName,
                    "user" to "dbuser",
                    "password" to "secret"  // TODO generate pwd
                )
            )
            resources.add(secret)
        }
        return resources
    }
}
