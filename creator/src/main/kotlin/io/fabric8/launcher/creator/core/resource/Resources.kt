package io.fabric8.launcher.creator.core.resource

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.propsOf

interface NamedProperties : BaseProperties {
    var name: String?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), NamedProperties {
        override var name: String? by _map
    }
}

interface Metadata : NamedProperties {
    var annotations: Properties?
    var labels: Properties?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : NamedProperties.Data(map), Metadata {
        override var annotations: Properties? by _map
        override var labels: Properties? by _map
    }
}

interface Parameter : NamedProperties {
    var description: String?
    var displayName: String?
    var value: Any?
    var required: Boolean?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : NamedProperties.Data(map), Parameter {
        override var description: String? by _map
        override var displayName: String? by _map
        override var value: Any? by _map
        override var required: Boolean? by _map
    }
}

interface Resource : BaseProperties {
    var apiVersion: String?
    var kind: String?
    var metadata: Metadata?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map), Resource {
        override var apiVersion: String? by _map
        override var kind: String? by _map
        override var metadata: Metadata? by _map
        fun metadata_(block: Metadata.Data.() -> Unit) {
            metadata = Metadata.build(block = block)
        }

        init {
            ensureObject(::metadata, Metadata::Data)
        }
    }
}

interface ListResource : Resource {
    var items: MutableList<Resource>

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : Resource.Data(map), ListResource {
        override var items: MutableList<Resource> by _map

        init {
            ensureList(::items, Resource::Data)
        }
    }
}

interface TemplateResource : Resource {
    var objects: MutableList<Resource>
    var parameters: MutableList<Parameter>

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : Resource.Data(map), TemplateResource {
        override var objects: MutableList<Resource> by _map
        override var parameters: MutableList<Parameter> by _map

        init {
            ensureList(::objects, Resource::Data)
            ensureList(::parameters, Parameter::Data)
        }
    }
}

class Resources(props: Properties = propsOf()) {
    private var ress: Resource

    init {
        ress = propsToResource(props)
    }

    companion object {
        private fun propsToResource(props: Properties): Resource {
            val kind = props["kind"] as? String
            return if ("List".equals(kind, true)) {
                ListResource.build(props)
            } else if ("Template".equals(kind, true)) {
                TemplateResource.build(props)
            } else {
                Resource.build(props)
            }
        }

        // Returns a list of all the resources found in the given object.
        // Will return the items of a List or the objects contained in a
        // Template. It will return a list of one if there's just a single
        // item that's neither a List nor a Template. And finally will
        // return an empty list if no resources were found at all.
        private fun asList(res: Resource): MutableList<Resource> {
            return when (res) {
                is ListResource -> res.items
                is TemplateResource -> res.objects
                else -> {
                    if (res.kind != null) {
                        mutableListOf(res)
                    } else {
                        mutableListOf()
                    }
                }
            }
        }

        // Takes a list of resources and turns them into a "List"
        private fun makeList(resources: List<Resource>): ListResource {
            return ListResource.build {
                apiVersion = "v1"
                kind = "List"
                items = resources.toMutableList()
            }
        }

        // Takes an list of resources and turns them into a "Template"
        private fun makeTemplate(resources: List<Resource>, params: List<Parameter>?): TemplateResource {
            val ps = params ?: mutableListOf()
            return TemplateResource.build {
                apiVersion = "v1"
                kind = "Template"
                parameters = ps.toMutableList()
                objects = resources.toMutableList()
            }
        }

        // Selects resources by their 'kind' property
        private fun selectByKind(res: List<Resource>, kind: String): List<Resource> {
            return res.filter { r -> r.kind?.toLowerCase() == kind.toLowerCase() }
        }

        // Selects resources by their 'metadata/name' property
        private fun selectByName(res: List<Resource>, name: String): List<Resource> {
            return res.filter { r -> r.metadata?.name == name }
        }

        // Selects the first resource that matches the given 'metadata/name' property
        private fun findByName(res: List<Resource>, name: String): Resource? {
            return res.find { r -> r.metadata?.name == name }
        }
    }

    // Returns an array of the separate resource items
    val items: List<Resource>
        get() = asList(ress)

    // Returns true if no resources were found in the given argument
    val isEmpty: Boolean
        get() = items.isEmpty()

    // Returns the wrapped object
    val json: Resource
        get() {
            return ress
        }

    // Returns the parameters (if any)
    val parameters: List<Parameter>
        get() {
            return (ress as? TemplateResource)?.parameters ?: listOf()
        }

    // Finds a parameter by name
    fun parameter(name: String): Parameter? {
        return parameters.find { p -> p.name == name }
    }

    // Turns the current resources into a List
    fun toList(): ListResource {
        val r = ress
        if (r is ListResource) {
            return r
        }
        val lr = makeList(items)
        ress = lr
        return lr
    }

    // Turns the current resources into a Template
    fun toTemplate(params: List<Parameter>? = mutableListOf()): TemplateResource {
        val r = ress
        if (r is TemplateResource) {
            return r
        }
        val tr = makeTemplate(items, params)
        ress = tr
        return tr
    }

    // Adds new resources from 'newres' to the wrapped object.
    // If the current wrapped object is a List the new resources will be added
    // to its items. If it's a Template they will be added to its objects. If
    // it's a single resource a List will be created containing that resource
    // plus all the new ones. If the current wrapped object is empty a new List
    // will be created if 'newres' has multiple resources or it will be set to
    // contain the single 'newres' item if there's only one.
    fun add(addres: Resource): Resources {
        var params: List<Parameter>? = null
        val additems = asList(addres)
        val r = ress
        if (r is ListResource) {
            val newitems = mutableListOf<Resource>()
            newitems.addAll(r.items)
            newitems.addAll(additems)
            r.items = newitems
        } else if (r is TemplateResource) {
            params = r.parameters
            val newobjects = mutableListOf<Resource>()
            newobjects.addAll(r.objects)
            newobjects.addAll(additems)
            r.objects = newobjects
        } else if (r.kind != null) {
            val newitems = mutableListOf(r)
            newitems.addAll(additems)
            ress = makeList(newitems)
        } else {
            if (additems.size > 1) {
                ress = makeList(additems)
            } else if (additems.size == 1) {
                ress = additems[0]
            }
        }

        // If there are any parameters merge them
        var resparams: List<Parameter>? = null
        if (addres is TemplateResource) {
            resparams = addres.parameters
        }
        if (params != null || resparams != null) {
            val newparams = mutableListOf<Parameter>()
            if (params != null) {
                newparams.addAll(params)
            }
            if (resparams != null) {
                newparams.addAll(resparams)
            }
            this.toTemplate().parameters = newparams
        }

        return this
    }

    fun add(newprops: Properties): Resources {
        return add(propsToResource(newprops))
    }

    fun add(newres: Resources): Resources {
        return add(newres.json)
    }

    // Adds the given parameter to the current list of parameters.
    // The resources will be turned into a Template first if necessary
    fun addParam(param: Parameter): Resources {
        val r = toTemplate()
        val newparams = mutableListOf<Parameter>()
        newparams.addAll(r.parameters)
        newparams.add(param)
        r.parameters = newparams
        return this
    }

    // Sets a new value for the given parameter.
    // An exception will be thrown if the parameter doesn't exist
    fun setParam(name: String, value: String): Resources {
        val p = parameter(name)
        if (p != null) {
            p.name = name
            p.value = value
            return this
        } else {
            throw NoSuchElementException("Parameter with name '$name'")
        }
    }

    // Sets a new value for the given parameter or adds a new parameter if it doesn't exist yet
    fun setOrAddParam(name: String, value: String): Resources {
        val p = parameter(name)
        if (p != null) {
            p.name = name
            p.value = value
            return this
        } else {
            return addParam(Parameter.build {
                this.name = name
                this.value = value
            })
        }
    }

    val builds: List<Resource>
        get() {
            return selectByKind(asList(ress), "build")
        }

    fun build(name: String): Resource? {
        return findByName(builds, name)
    }

    val buildConfigs: List<Resource>
        get() {
            return selectByKind(asList(ress), "buildconfig")
        }

    fun buildConfig(name: String): Resource? {
        return findByName(buildConfigs, name)
    }

    val configMaps: List<Resource>
        get() {
            return selectByKind(asList(ress), "configmap")
        }

    fun configMap(name: String): Resource? {
        return findByName(configMaps, name)
    }

    val deployments: List<Resource>
        get() {
            return selectByKind(asList(ress), "deployment")
        }

    fun deployment(name: String): Resource? {
        return findByName(deployments, name)
    }

    val deploymentConfigs: List<Resource>
        get() {
            return selectByKind(asList(ress), "deploymentconfig")
        }

    fun deploymentConfig(name: String): Resource? {
        return findByName(deploymentConfigs, name)
    }

    val imageStreamImages: List<Resource>
        get() {
            return selectByKind(asList(ress), "imagestreamimage")
        }

    fun imageStreamImage(name: String): Resource? {
        return findByName(imageStreamImages, name)
    }

    val imageStreams: List<Resource>
        get() {
            return selectByKind(asList(ress), "imagestream")
        }

    fun imageStream(name: String): Resource? {
        return findByName(imageStreams, name)
    }

    val imageStreamTags: List<Resource>
        get() {
            return selectByKind(asList(ress), "imagestreamtag")
        }

    fun imageStreamTag(name: String): Resource? {
        return findByName(imageStreamTags, name)
    }

    val persistentVolumes: List<Resource>
        get() {
            return selectByKind(asList(ress), "persistentvolume")
        }

    fun persistentVolume(name: String): Resource? {
        return findByName(persistentVolumes, name)
    }

    val persistentVolumeClaims: List<Resource>
        get() {
            return selectByKind(asList(ress), "persistentvolumeclaim")
        }

    fun persistentVolumeClaim(name: String): Resource? {
        return findByName(persistentVolumeClaims, name)
    }

    val roles: List<Resource>
        get() {
            return selectByKind(asList(ress), "role")
        }

    fun role(name: String): Resource? {
        return findByName(roles, name)
    }

    val roleBindings: List<Resource>
        get() {
            return selectByKind(asList(ress), "rolebinding")
        }

    fun roleBinding(name: String): Resource? {
        return findByName(roleBindings, name)
    }

    val routes: List<Resource>
        get() {
            return selectByKind(asList(ress), "route")
        }

    fun route(name: String): Resource? {
        return findByName(routes, name)
    }

    val secrets: List<Resource>
        get() {
            return selectByKind(asList(ress), "secret")
        }

    fun secret(name: String): Resource? {
        return findByName(secrets, name)
    }

    val services: List<Resource>
        get() {
            return selectByKind(asList(ress), "service")
        }

    fun service(name: String): Resource? {
        return findByName(services, name)
    }
}

