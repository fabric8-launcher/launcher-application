# Fabric8-Launcher Backend Contributing Guide

If you're looking for a development guide for the Fabric8-Launcher Backend, first read [this document](./README.md).

## First Steps

### Prerequisites

* Java 8+
* Maven 3.5.3+
* Git

### Git Workflow

To contribute to the Fabric8-Launcher Backend, fork this repository to your own Git, clone your fork, commit your work on topic branches, and make pull requests.

If you don't have the Git client (`git`), get it from: <http://git-scm.com/>

Here are the steps in detail:

__1.__ [Fork](https://github.com/fabric8-launcher/launcher-backend/fork) the project. This creates a the project in your own Git with the default remote name 'origin'.

__2.__ Clone your fork. This creates and populates a directory in your local file system.

```bash
$ git clone git@github.com:<your-username>/launcher-backend.git`
```

__3.__ Add the remote `upstream` repository so you can fetch any changes to the original forked repository.

```bash
$ git remote add upstream git@github.com:fabric8-launcher/launcher-backend.git
```

__4.__ Get the latest files from the `upstream` repository.

```bash
$ git fetch upstream
```

__5.__ Create a new topic branch to contain your features, changes, or fixes. For example with `launcher-123` as branch name:

```bash
$ git checkout -b launcher-123 upstream/master
```

__6.__ Contribute new code or make changes to existing files. If you're using IntelliJ IDEA, be sure to format your code using the provided [Backend Code Formatter](https://raw.githubusercontent.com/fabric8-launcher/launcher-backend/master/ide-configs/idea/fabric8-launcher.xml)).

__7.__ Use the `git add` command to add new or changed file contents to the staging area.

Interactively stage hunks. This gives you a chance to review the difference before adding modified contents to the index.

```bash
$ git add -p
```

Stages new and modified, __without deleted__

```bash
$ git add .
```

Stages __All__

```bash
$ git add -A
```

__8.__ Use the git status command to view the status of the files in the directory and in the staging area and ensure that all modified files are properly staged:

```bash
$ git status
```

__9.__ Commit your changes to your local topic branch following the [commit message conventions](./COMMIT_MSG.md).

```bash
$ git commit -m 'feat(core): Does something cool

Here is the reason why I made this cool thing.

This cool thing affect this, this and this.

Closes #123
'
```


__10.__ If you made multiple commits for one change, or if there were any merge commits created when you pulled from upstream, you can use [interactive rebase](https://git-scm.com/book/en/v2/Git-Tools-Rewriting-History) to get a beautiful contribution commit log:

Here is an example to rebase the last 5 commits

```bash
$ git rebase -i HEAD~5
```

Change 'pick' to 'f' for each commit you wish to fixup upwards. (If you do not change a line, it will not be modified.)

```
        1 pick a225b3d test(core): Test that cool thing
        2 pick ade2b1a feat(core): Does something cool
        3 f c3ae0a2 almost done
        4 f c863bfb did more work
        5 f af793ae Started working on something cool
        6 pick 368bbb9 docs(core): Previous commit written by someone else
```

Once you are done, your commits should look like this:

```
        1 a225b3d test(core): Test that cool thing
        2 ade2b1a feat(core): Does something cool
        3 368bbb9 docs(core): Previous commit written by someone else
```

You can view your commits by typing:

```bash
$ git log
```

__11.__ Push your local topic branch to your github forked repository. This will create a branch on your Git fork repository with the same name as your local topic branch name.

```bash
$ git push origin HEAD
```

IMPORTANT: The above command assumes
- your remote repository is named 'origin'. You can verify your forked remote repository name using the command _git remote -v_.
- the branch name is `launcher-123` as an example.

__12.__ Browse to the <topic-branch-name> branch on your forked Git repository and [Create a Pull Request](https://help.github.com/articles/creating-a-pull-request/). Give it a clear title and description.

## Keep your PR up-to-date

Your branch must be up-to-date with `upstream/master` in order to be merged.

```bash
$ git fetch
$ git status
```

If your branch is up-to-date, you will see this message `Your branch is ahead of 'upstream/master' by [n] commit.`.

If it is out of date, you will see this message `Your branch and 'upstream/master' have diverged`.

Please follow these instructions to make it up-to-date:

__1.__ Be sure that you have a clean working tree.

```bash
$ git status
[...]

nothing to commit, working tree clean
```

__2.__ Make your commit log clean by following the __10.__ in the workflow instructions.

__3.__ Rebase from `upstream/master` in order to rewind head and replay your work on top of it...

```bash
$ git rebase upstream/master
```

Resolve conflicts if needed and continue rebase `$ git rebase --continue`.

__4.__ Force push you changes to origin.

```bash
$ git push -f origin
```

Note: In our opinion, forced pushes are of course a "really bad thing" except when dealing with PRs :)

## Build, Test and Run

Read [this document](./README.md).

## Choosing issues to work on

If you're wondering what issues would be suitable when you're just getting started, we recommend taking a look at the [issues](https://github.com/fabric8-launcher/launcher-backend/issues) with the '[good first issue](https://github.com/fabric8-launcher/launcher-backend/labels/good%20first%20issue)' and '[help wanted](https://github.com/fabric8-launcher/launcher-backend/labels/help%20wanted)' labels.

## License Information and Contributor Agreement

* There is no need to sign a contributor agreement to contribute to the Fabric8-Launcher.
* [TODO complete the license information]
