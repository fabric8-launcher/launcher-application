package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.base.JsonUtils.*
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DeploymentDescriptorTest {

    @Test
    fun `apply capabilities`() {
        val value =
            "{\"project\":{\"application\":\"assorted-cause\",\"parts\":[{\"category\":\"backend\",\"shared\":{\"runtime\":{\"name\":\"nodejs\",\"version\":\"community\"}},\"capabilities\":[{\"module\":\"health\"},{\"module\":\"database\"},{\"module\":\"rest\"},{\"module\":\"welcome\"}]}]},\"gitRepository\":\"assorted-cause\",\"gitOrganization\":\"\",\"clusterId\":\"openshift-v3\",\"projectName\":\"assorted-cause\"}"
        val deploymentDescriptor = toDeploymentDescriptor(value)
        Assertions.assertThat(deploymentDescriptor).isNotNull
        // TODO: Call withDeployment to reproduce the error
    }

    private fun toDeploymentDescriptor(json: String): DeploymentDescriptor {
        val app = createObjectNode()
        app.set("applications", createArrayNode().add(readTree(json)))
        val project = toMap(app)
        return DeploymentDescriptor.build(project)
    }

}