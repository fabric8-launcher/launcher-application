package io.fabric8.launcher.creator.core.deploy

import io.fabric8.launcher.creator.core.BaseProperties
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.propsOf

interface CapabilityDescriptor : BaseProperties {
    val module: String                  // The name of the applied capability
    val props: Properties?              // The properties to pass to the capability
    val extra: Properties?              // Any properties the capability might return

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), CapabilityDescriptor {
        override var module: String by _map                  // The name of the applied capability
        override var props: Properties? by _map              // The properties to pass to the capability
        override var extra: Properties? by _map              // Any properties the capability might return
    }
}

interface PartDescriptor : BaseProperties {
    val subFolderName: String?          // The name of the subFolderName
    val shared: Properties?             // Any shared properties that will be passed to all capabilities
    val extra: Properties?              // Any shared properties returned by capabilities
    var capabilities: MutableList<CapabilityDescriptor>   // All capabilities that are part of the subFolderName

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), PartDescriptor {
        override var subFolderName: String? by _map      // The name of the subFolderName
        override var shared: Properties? by _map         // Any shared properties that will be passed to all capabilities
        override var extra: Properties? by _map          // Any shared properties returned by capabilities
        override var capabilities: MutableList<CapabilityDescriptor> by _map   // All capabilities that are part of the subFolderName

        init {
            ensureList(::capabilities, CapabilityDescriptor::Data)
        }
    }
}

interface ApplicationDescriptor : BaseProperties {
    val application: String             // The name of the application
    val extra: Properties?              // Any application properties unused by the creator itself
    var parts: MutableList<PartDescriptor>     // Parts are groups of capabilities that make up the application

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), ApplicationDescriptor {
        override var application: String by _map             // The name of the application
        override var extra: Properties? by _map              // Any application properties unused by the creator itself
        override var parts: MutableList<PartDescriptor> by _map     // Parts are groups of capabilities that make up the application

        init {
            ensureList(::parts, PartDescriptor::Data)
        }
    }
}

interface DeploymentDescriptor : BaseProperties {
    var applications: MutableList<ApplicationDescriptor>   // All applications that are part of the deployment

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), DeploymentDescriptor {
        override var applications: MutableList<ApplicationDescriptor> by _map   // All applications that are part of the deployment

        init {
            ensureList(::applications, ApplicationDescriptor::Data)
        }
    }
}
