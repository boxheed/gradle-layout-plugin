[![CircleCI](https://circleci.com/gh/boxheed/gradle-layout-plugin/tree/master.svg?style=shield)](https://circleci.com/gh/boxheed/gradle-layout-plugin/tree/master)

# Gradle Layout Plugin

A Gradle plugin providing the ability to specify the folder and file layout of the project, optionally including the contents of files or fetching remote layout descriptors.

# Usage

```groovy
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // The layout plugin.
        classpath 'com.fizzpod:gradle-layout-plugin:6+'
    }
}

apply plugin: 'com.fizzpod.layout'

layout {
    // Set default overwrite behavior (defaults to false)
    overwrite false

    // Create an empty file
    file 'filename1.txt'
    // Create an empty file in subfolders
    file 'folder1/folder1_1/filename2.txt'
    // Create a file in a subfolder with some content
    file('folder1/folder1_1/filename3.txt') {
        "I am some content"
    }
    // Create a file in a subfolder with multiline content
    file('folder1/folder1_1/filename4.txt') {
        """I am some content
Split over
multiple lines"""
    }
    // Create or overwrite a file fetched from a remote layout or URL
    file '.github/workflows/ci.yml', [remote: true, overwrite: true]
    file '.github/scripts/run.sh', [remote: true, overwrite: true, executable: true]

    // Apply a named layout from the remote layout repository
    name 'common'

    path 'folder2/folder2_1'
}
```

# Tasks

The plugin exposes the following tasks:
- `applyLayout`: creates or updates (when `overwrite: true`) the specified layout. By default, if a file exists it will not overwrite it unless `overwrite: true` is configured globally or on the individual file entry.
- `checkLayout`: verifies that configured directories and files exist and that `overwrite: true` files match their expected contents. Remote checks are skipped with a warning when Gradle is offline (`--offline` or `-Playout.offline=true`).
- `createLayout`: deprecated alias for `applyLayout`.

# Bootstrapping an Empty Repository

You can bootstrap an empty directory with a layout from the command line without creating a `build.gradle` first:

```sh
# Inside an empty repository (uses current directory name as project name)
curl -sSL https://raw.githubusercontent.com/boxheed/gradle-layout-plugin/master/bootstrap.sh | sh -s -- --name gradle-plugin-bootstrap

# Or target a specific directory
curl -sSL https://raw.githubusercontent.com/boxheed/gradle-layout-plugin/master/bootstrap.sh | sh -s -- --name gradle-plugin-bootstrap --dir my-plugin
```

Run `sh bootstrap.sh --help` for all options (`--url`, `--remote`, `--overwrite`, `--dry-run`, `--plugin-version`, `--project-name`, `--project-group`, `--project-version`, `--project-description`, `--non-interactive`).

## Bootstrap vs. Ongoing Layout Pattern

Because `applyLayout` recreates missing files (even with `overwrite: false`), keep one-time scaffolding separate from your ongoing shared layout:
- **Bootstrap layout** (e.g. `gradle-plugin-bootstrap`): composes the base layout (`"names": ["gradle-plugin"]`) and adds initial project files (`build.gradle`, `settings.gradle`, `.gitkeep` placeholders, `init-gradle.sh`) used only once when creating a new repository.
- **Ongoing layout** (e.g. `gradle-plugin`): referenced in `build.gradle` / `gradle-plugin-opinion` for `applyLayout` and `checkLayout`, so deleted one-time files like `.gitkeep` or unused source directories are never recreated.
