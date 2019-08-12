package io.fabric8.launcher.creator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import io.fabric8.launcher.creator.catalog.GeneratorInfo
import io.fabric8.launcher.creator.core.catalog.enumById

class CreatorList : NoRunCliktCommand(name="list", help="Lists possible values for capabilities and runtimes") {
    init {
        context { allowInterspersedArgs = false }
    }
}

class ListCapabilities : CliktCommand(name="capabilities", help = "Lists possible values for capabilities") {
    override fun run() {
        GeneratorInfo
            .capabilities()
            .map { it.name.substring(11) }
            .sorted()
            .forEach { println(it) }
    }
}

class ListRuntimes : CliktCommand(name = "runtimes", help = "Lists possible values for runtimes") {
    override fun run() {
        enumById("runtime.name").map { it.id }.sorted().forEach { println(it) }
    }
}
