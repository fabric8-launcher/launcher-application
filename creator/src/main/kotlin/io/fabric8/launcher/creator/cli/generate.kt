package io.fabric8.launcher.creator.cli

import com.github.ajalt.clikt.core.CliktCommand
import io.fabric8.launcher.creator.core.resource.generate

class Generate : CliktCommand(help = "Generates all the OpenShift tempaltes needed for the Creator to work") {
    override fun run() {
        generate()
    }
}
