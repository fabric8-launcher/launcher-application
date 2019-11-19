package it

import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

fun waitForFirstBuild(part: Part) {
    // First we wait for the first build to appear
    // (if we try to start our build sooner it will silently fail)
    System.out.println("Waiting for build system to ready up")
    for (i in 1..60) {
        try {
            runTestCmd("oc", "get", "build/" + getServiceName(part) + "-1", "--template={{.status.phase}}")
            System.out.println("ok")
            break
        } catch (ex: Exception) {
            if (!ex.localizedMessage.toLowerCase().contains("(notfound)")) {
                System.out.println("failed")
                throw ex
            }
            if (!isDryRun()) {
                sleep(5000)
                System.out.println("${i * 5} seconds have passed...")
            }
        }
    }
    // Then we cancel that first build which will fail anyway
    try {
        runTestCmd("oc", "cancel-build", getServiceName(part) + "-1")
    } catch (ex: Exception) {
        // Ignore any errors
    }
}

fun waitForProject(part: Part) {
    // We wait for the deployment to spin up our application
    waitForAppStart(part)
    // Wait for the app to respond
    waitForAppResponse(part)
}

fun waitForAppStart(part: Part) {
    System.out.println("Waiting for application to start...")
    for (i in 1..60) {
        try {
            runTestCmd("oc", "wait", "dc/" + getServiceName(part), "--timeout=15s", "--for", "condition=available")
            System.out.println("ok")
            break
        } catch (ex: Exception) {
            if (!ex.localizedMessage.toLowerCase().contains("error: timed out")) {
                System.out.println("failed")
                throw ex
            }
            System.out.println("${i * 15} seconds have passed...")
        }
    }
}

fun waitForAppResponse(part: Part) {
    System.out.println("Waiting for application to respond...")
    if (!isDryRun()) {
        for (i in 1..10) {
            val url = "http://${getRouteHost(getServiceName(part))}/health"
            try {
                with(URL(url).openConnection() as HttpURLConnection) {
                    if (responseCode == 200) {
                        System.out.println("ok ($responseCode)")
                        return
                    }
                }
            } catch (ex: UnknownHostException) {
                // We can safely ignore this, the route probably isn't ready yet
            }
            sleep(5000)
            System.out.println("${i * 5} seconds have passed...")
        }
    }
}

fun getRouteHost(name: String): String {
    return runTestCmd("oc", "get", "route", name, "--template", "{{.spec.host}}")
}