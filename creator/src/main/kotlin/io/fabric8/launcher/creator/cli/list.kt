package io.fabric8.launcher.creator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo
import io.fabric8.launcher.creator.core.catalog.enum

class CreatorList : NoRunCliktCommand(name="list", help="Lists possible values for capabilities and runtimes") {
    init {
        context { allowInterspersedArgs = false }
    }
}

class ListCapabilities : CliktCommand(name="capabilities", help = "Lists possible values for capabilities") {
    override fun run() {
        CapabilityInfo.values().sorted().forEach { println(it) }
    }
}

class ListRuntimes : CliktCommand(name = "runtimes", help = "Lists possible values for runtimes") {
    override fun run() {
        enum("runtime.name").map { it.id }.sorted().forEach { println(it) }
    }
}
