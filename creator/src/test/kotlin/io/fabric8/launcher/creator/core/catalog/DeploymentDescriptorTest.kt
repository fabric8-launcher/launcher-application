package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.deploy.*
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.toRuntime
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DeploymentDescriptorTest {

    @Test
    fun `apply capabilities`() {
        val deployment = DeploymentDescriptor.build {
            applications = mutableListOf(ApplicationDescriptor.build {
                application = "assorted-cause"
                parts = mutableListOf(PartDescriptor.build {
                    shared = propsOf(
                        "runtime" to toRuntime("nodejs/community")
                    )
                    capabilities = mutableListOf(
                        CapabilityDescriptor.build {
                            module = "health"
                        },
                        CapabilityDescriptor.build {
                            module = "database"
                        },
                        CapabilityDescriptor.build {
                            module = "rest"
                        },
                        CapabilityDescriptor.build {
                            module = "welcome"
                        }
                    )
                })
            })
        }
        withDeployment(deployment) {
            Assertions.assertThat(resolve("package.json")).exists()
        }
    }
}