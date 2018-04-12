# Git Commit/Pull-Request Message Guide

## The reasons for this guide:

- automatic generating of the changelog
- simple navigation through git history (e.g. ignoring style changes)

## Format of the commit message:

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Example commit message:

```
fix(git): repository endpoint returns forks as well

Add `fork:true` to the GitHub search api query to fix the bug.

Closes #285
```

## Message subject (first line)

The first line cannot be longer than 70 characters, the second line is always blank and other lines should be wrapped at 80 characters. The type and scope should always be lowercase as shown below.

### Allowed <type> values:

- feat (new feature for the user, not a new feature for build script)
- fix (bug fix for the user, not a fix to a build script)
- docs (changes to the documentation)
- style (formatting, missing semi colons, etc; no production code change)
- refactor (refactoring production code, eg. renaming a variable)
- test (adding missing tests, refactoring tests; no production code change)
- chore (updating grunt tasks etc; no production code change)

### Example <scope> values:

- base
- core
- web
- osio-addon
- git
- keycloak
- etc.

The `<scope>` can be empty (e.g. if the change is a global or difficult to assign to a single component), in which case the parentheses are omitted.

## Message body

Here, it should be nice if you include motivation for the change and anything you think relevant regarding your commit.

For more tips about message body, you can [read this article](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).

## Message footer

In the footer you should mention issues and any breaking changes.

### Referencing issues

Closed issues should be listed on a separate line in the footer prefixed with "Closes" keyword like this:

```
Closes #234
```

or in the case of multiple issues:

```
Closes #123, #245, #992
```

### Breaking changes

All breaking changes have to be mentioned in footer with the description of the change, justification and migration notes.

For example:

```
BREAKING CHANGE:

All environment variables are now prefixed with `LAUNCHER_`.

Use `./launcher-env-template.sh` to update your local environment.
```

# Thanks

Thanks to [source](http://karma-runner.github.io/2.0/dev/git-commit-msg.html) for the original text.