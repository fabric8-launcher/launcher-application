package io.fabric8.launcher.creator.core.analysis

import io.fabric8.launcher.creator.core.runCmd
import java.nio.file.Files
import java.nio.file.Path

fun cloneGitRepo(targetDir: Path, gitRepoUrl: String, gitRepoBranch: String?) {
    // Shallow-clone the repository
    runCmd(
        "git",
        "clone",
        // Work-around for problem in older Gits
        // https://github.com/git/git/commit/92bcbb9b338dd27f0fd4245525093c4bce867f3d
        "-cuser.name=dummy",
        "-cuser.email=dummy",
        // Work-around to force Git never to ask for passwords
        "-ccore.askPass",
        gitRepoUrl,
        "--depth=1",
        "--single-branch",
        "--branch=${gitRepoBranch ?: "master"}",
        targetDir.toAbsolutePath().toString()
    )
}

fun removeGitFolder(targetDir: Path) {
    targetDir.resolve(".git").toFile().deleteRecursively()
}

fun listBranchesFromGit(gitRepoUrl: String): List<String> {
    // Git the list of branches and tags from the remote Git repository
    val out = runCmd(
        "git",
        // Work-around for problem in older Gits
        // https://github.com/git/git/commit/92bcbb9b338dd27f0fd4245525093c4bce867f3d
        "-c", "user.name=dummy",
        "-c", "user.email=dummy",
        // Work-around to force Git never to ask for passwords
        "-c", "core.askPass=/bin/echo",
        "ls-remote",
        "--heads",
        "--tags",
        gitRepoUrl
    )
    val regex = """refs/.*?/(.*)""".toRegex(RegexOption.MULTILINE)
    return regex
        .findAll(out)
        .map { it.groupValues[1] }
        .filter { !it.endsWith("^{}") }
        .toList()
}

fun <T> withGitRepo(gitRepoUrl: String, gitRepoBranch: String? = null, block: Path.() -> T): T {
    // Create temp dir
    val td = Files.createTempDirectory("creator")
    try {
        // Shallow-clone the repository
        cloneGitRepo(td, gitRepoUrl, gitRepoBranch)
        // Now execute the given code block
        return block.invoke(td)
    } finally {
        // In the end clean everything up again
        td.toFile().deleteRecursively()
    }
}
