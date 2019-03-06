package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*

private val livenessProbe = propsOf(
    "initialDelaySeconds" to 120,
    "exec" to propsOf(
        "command" to listOf(
            "/usr/libexec/check-container",
            "--live"
        )
    )
)

private val readinessProbe = propsOf(
    "initialDelaySeconds" to 5,
    "exec" to propsOf(
        "command" to listOf(
            "/usr/libexec/check-container"
        )
    )
)

interface DatabasePostgresqlProps : BaseGeneratorProps, DatabaseSecretRef {
    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), DatabasePostgresqlProps {
        override var secretName: String by _map
    }
}

class DatabasePostgresql(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dpprops = DatabasePostgresqlProps.build(props)
        // Check that the database doesn"t already exist
        if (resources.service(dpprops.serviceName) == null) {
            // Create the database resource definitions
            val res = newApp(dpprops.serviceName, dpprops.application, IMAGE_POSTGRESQL, null, envOf(
                "POSTGRESQL_DATABASE" to propsOf("secret" to dpprops.secretName, "key" to "database"),
                "POSTGRESQL_USER" to propsOf("secret" to dpprops.secretName, "key" to "user"),
                "POSTGRESQL_PASSWORD" to propsOf("secret" to dpprops.secretName, "key" to "password")
            ))
            setMemoryLimit(res, "512Mi")
            setCpuLimit(res, "1")
            setHealthProbe(res, "livenessProbe", livenessProbe)
            setHealthProbe(res, "readinessProbe", readinessProbe)
            resources.add(res)
        }

        val exProps = propsOf(
            "image" to IMAGE_POSTGRESQL,
            "service" to dpprops.secretName
        )
        extra["databaseInfo"] = exProps

        return resources
    }
}
