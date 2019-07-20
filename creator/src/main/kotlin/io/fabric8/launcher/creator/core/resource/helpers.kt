package io.fabric8.launcher.creator.core.resource

import io.fabric8.launcher.creator.core.*

// Updates the environment variables for the BuildConfig selected
// by 'bcName' with the given key/values in the object 'env'. The values
// are either simple strings or they can be objects themselves in which
// case they are references to keys in a ConfigMap or a Secret.
fun setBuildEnv(res: Resources, env: Environment?, bcName: String? = null): Resources {
    if (env != null && res.buildConfigs.isNotEmpty()) {
        val bc = if (bcName != null) res.buildConfig(bcName) else res.buildConfigs[0]
        val bcss = bc?.pathGet<Properties>("spec.strategy.sourceStrategy")
        if (bcss != null) {
            val newenv = mergeEnv(bcss["env"] as List<Properties>?, convertObjectToEnvWithRefs(env))
            bcss.set("env", newenv)
        }
    }
    return res
}

// Updates the environment variables for the DeploymentConfig selected
// by 'dcName' with the given key/values in the object 'env'. The values
// are either simple strings or they can be objects themselves in which
// case they are references to keys in a ConfigMap or a Secret.
fun setDeploymentEnv(res: Resources, env: Environment?, dcName: String? = null): Resources {
    if (env != null && res.deploymentConfigs.isNotEmpty()) {
        val dc = if (dcName != null) res.deploymentConfig(dcName) else res.deploymentConfigs[0]
        val dcss = dc?.pathGet<Properties>("spec.template.spec.containers[0]")
        if (dcss != null) {
            val newenv = mergeEnv(dcss["env"] as List<Properties>?, convertObjectToEnvWithRefs(env))
            dcss.set("env", newenv)
        }
    }
    return res
}

// Merges the vars from the `env` object with the `targetEnv` object.
// Both objects should be lists of DeploymentConfig environment definitions
fun mergeEnv(targetEnv: List<Properties>?, env: List<Properties>): List<Properties> {
    val tenv = targetEnv ?: mutableListOf()
    return tenv.filter { t -> env.none { e -> e["name"] == t["name"] } } + env
}

// Converts an object with key/values to an array of DeploymentConfig environment definitions.
// If the value of a key/value pair is a string a simple object with name/value properties will
// be created. In the case that the value is an object it will be assumed to contain a reference
// to a key in a ConfigMap or Secret or a reference to a field in the resources. In the case
// of the ConfigMap or Secret the object must have a `key` property and either a `secretKeyRef`
// with the name of a Secret or a `configMapKeyRef` with the name of a ConfigMap. In the case of
// a `fieldRef` the object just needs to contain a single `field` property that holds the path
// to the field.
fun convertObjectToEnvWithRefs(env: Environment): List<Properties> {
    return env.entries.map { e ->
        val envKey = e.key
        val envValue = e.value
        if (envValue is Map<*, *>) {
            val valueFrom = propsOf()
            val envVar = propsOf(
                "name" to envKey,
                "valueFrom" to valueFrom
            )
            when {
                envValue.containsKey("secret") -> valueFrom["secretKeyRef"] = propsOf(
                    "name" to envValue["secret"],
                    "key" to envValue["key"]
                )
                envValue.containsKey("configMap") -> valueFrom["configMapKeyRef"] = propsOf(
                    "name" to envValue["configMap"],
                    "key" to envValue["key"]
                )
                envValue.containsKey("field") -> valueFrom["fieldRef"] = propsOf(
                    "fieldPath" to envValue["field"]
                )
                else -> throw Exception("Missing ENV value 'secret', 'configMap' or 'field' property")
            }
            envVar
        } else {
            propsOf(
                "name" to envKey,
                "value" to envValue.toString()
            )
        }
    }
}

// Updates the contextDir in the source strategy of the BuildConfig selected
// by 'bcName' with the given path.
fun setBuildContextDir(res: Resources, contextDir: String?, bcName: String? = null): Resources {
    if (contextDir != null && res.buildConfigs.isNotEmpty()) {
        val bc = if (bcName != null) res.buildConfig(bcName) else res.buildConfigs[0]
        bc?.pathPut("spec.source.contextDir", contextDir)
    }
    return res
}

private fun setComputeResources(res: Resources, category: String, name: String, value: String, dcName: String? = null): Resources {
    if (res.deploymentConfigs.isNotEmpty()) {
        val dc = if (dcName != null) res.deploymentConfig(dcName) else res.deploymentConfigs[0]
        dc?.pathGet<Properties>("spec.template.spec.containers[0]")?.pathPut("resources.$category.$name", value)
    }
    return res
}

// Sets the cpu limits for the DeploymentConfig selected by 'dcName'
// with the given ComputeResources for cpu.
fun setCpuLimit(res: Resources, limit: String, dcName: String? = null): Resources {
    return setComputeResources(res, "limits", "cpu", limit, dcName)
}

// Sets the cpu requests for the DeploymentConfig selected by 'dcName'
// with the given ComputeResources for cpu.
fun setCpuRequest(res: Resources, request: String, dcName: String? = null): Resources {
    return setComputeResources(res, "requests", "cpu", request, dcName)
}

// Sets the memory limits for the DeploymentConfig selected by 'dcName'
// with the given ComputeResources for memory.
fun setMemoryLimit(res: Resources, limit: String, dcName: String? = null): Resources {
    return setComputeResources(res, "limits", "memory", limit, dcName)
}

// Sets the memory requests for the DeploymentConfig selected by 'dcName'
// with the given ComputeResources for memory.
fun setMemoryRequest(res: Resources, request: String, dcName: String? = null): Resources {
    return setComputeResources(res, "requests", "memory", request, dcName)
}

// Sets the given health check probe for the DeploymentConfig selected by 'dcName'.
fun setHealthProbe(res: Resources, probeName: String, probe: Properties?, dcName: String? = null): Resources {
    if (probe != null && res.deploymentConfigs.isNotEmpty()) {
        val dc = if (dcName != null) res.deploymentConfig(dcName) else res.deploymentConfigs[0]
        dc?.pathGet<Properties>("spec.template.spec.containers[0]")?.set(probeName, probe)
    }
    return res
}

// Sets the readiness health checks for the DeploymentConfig selected by 'dcName' to a path that should be periodically queried
fun setReadinessPath(res: Resources,
                     path: String,
                     dcName: String? = null): Resources {
    val readinessProbe = propsOf(
        "httpGet" to propsOf(
            "path" to path,
            "port" to 8080,
            "scheme" to "HTTP"
        ),
        "initialDelaySeconds" to 5,
        "timeoutSeconds" to 3
    )
    setHealthProbe(res, "readinessProbe", readinessProbe, dcName)
    return res
}

// Sets the liveness health checks for the DeploymentConfig selected by 'dcName' to a path that should be periodically queried
fun setLivenessPath(
    res: Resources,
    path: String,
    dcName: String? = null
): Resources {
    val livenessProbe = propsOf(
        "httpGet" to propsOf(
            "path" to path,
            "port" to 8080,
            "scheme" to "HTTP"
        ),
        "initialDelaySeconds" to 120,
        "timeoutSeconds" to 3
    )
    setHealthProbe(res, "livenessProbe", livenessProbe, dcName)
    return res
}

// Sets the default readiness check to "/health"
fun setDefaultReadiness(res: Resources, dcName: String? = null): Resources {
    setReadinessPath(res, "/health", dcName)
    return setLivenessPath(res, "/health", dcName)
}

// Sets the default liveness check to "/health"
fun setDefaultLiveness(res: Resources, dcName: String? = null): Resources {
    setReadinessPath(res, "/health", dcName)
    return setLivenessPath(res, "/health", dcName)
}

// Sets the "app" label on all resources to the given value
fun setAppLabel(res: Resources, appLabel: String) : Resources {
    return setAppLabel(res, propsOf("app" to appLabel))
}

// Sets the "app" label on all resources to the given value
fun setAppLabel(res: Resources, appLabels: Properties) : Resources {
    res.items.forEach { r ->
        appLabels.entries.forEach { entry -> r.pathPut("metadata.labels.${entry.key}", entry.value) }
    }
    return res
}

// Returns a list of resources that when applied will create
// an instance of the given image or template. Any environment
// variables being passed will be applied to any `DeploymentConfig`
// and `BuildConfig` resources that could be found in the image
fun newApp(appName: String,
           appLabel: String,
           imageName: String,
           sourceUri: String? = null,
           env: Environment? = envOf()): Resources {
    return newApp(appName, propsOf("app" to appLabel), imageName, sourceUri, env)
}

fun newApp(appName: String,
           appLabels: Properties,
           imageName: String,
           sourceUri: String?,
           env: Environment? = envOf()): Resources {
    val appRes = readTemplate(imageName, appName, null, sourceUri)
    setAppLabel(appRes, appLabels)
    setBuildEnv(appRes, env)
    setDeploymentEnv(appRes, env)
    return appRes
}

fun newRoute(res: Resources,
             appName: String,
             appLabel: String,
             serviceName: String,
             port: Int = -1): Resources {
    return newRoute(res, appName, propsOf("app" to appLabel), serviceName, port)
}

fun newRoute(res: Resources,
             appName: String,
             appLabels: Properties,
             serviceName: String, port: Int = -1): Resources {
    val portName = if (port == -1) {
        res.service(serviceName)?.pathGet<String>("spec.ports[0].name")
    } else {
        res.service(serviceName)?.pathGet<List<Properties>>("spec.ports")?.find { p -> p["port"] == port }?.get("name")
    }
    res.add(
        propsOf(
            "apiVersion" to "v1",
            "kind" to "Route",
            "metadata" to propsOf(
                "name" to appName,
                "labels" to appLabels
            ),
            "spec" to propsOf(
                "port" to propsOf(
                    "targetPort" to portName
                ),
                "to" to propsOf(
                    "kind" to "Service",
                    "name" to serviceName
                )
            )
        )
    )
    return res
}

fun newService(res: Resources,
               appName: String,
               appLabel: String,
               serviceName: String): Resources {
    return newService(res, appName, propsOf("app" to appLabel), serviceName)
}

fun newService(res: Resources,
               appName: String,
               appLabel: Properties,
               serviceName: String): Resources {
    TODO("not implemented")
}
