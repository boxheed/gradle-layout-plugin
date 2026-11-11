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

The plugin exposes a single task `createLayout` which creates the specified layout. By default, if a file exists it will not overwrite it unless `overwrite: true` is configured globally or on the individual file entry.
