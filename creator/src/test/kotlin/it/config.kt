package it

fun isDryRun(): Boolean {
//    return true
    return System.getProperty("launcher.it.dryrun", "false").toBoolean()
}

fun isNoBuild(): Boolean {
    return System.getProperty("launcher.it.nobuild", "false").toBoolean()
}

fun isVerbose(): Boolean {
    return System.getProperty("launcher.it.verbose", "false").toBoolean()
}

fun getRuntimeOverrides(): List<String>? {
    return System.getProperty("launcher.it.runtimes")?.split(',')
}

fun getCapabilityOverrides(): List<String>? {
    return System.getProperty("launcher.it.capabilities")?.split(',')?.map { "capability-$it" }
}
