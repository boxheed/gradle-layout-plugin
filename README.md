# Gradle Layout Plugin

[![CircleCI](https://circleci.com/gh/boxheed/gradle-layout-plugin/tree/master.svg?style=shield)](https://circleci.com/gh/boxheed/gradle-layout-plugin/tree/master)

A Gradle plugin providing the ability to specify the folder and file layout of the project, optionally including the contents of file.

## Usage

### 1. Apply the Plugin

In your `build.gradle` file, apply the plugin:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.fizzpod:gradle-layout-plugin:1.0.0' // Use the correct version
    }
}

apply plugin: 'com.fizzpod.layout'
```

### 2. Configure the Layout

Define the desired project structure in the `layout` block:

```groovy
layout {
    // Create an empty file
    file 'filename1.txt'
    
    // Create an empty file in subfolders
    file 'folder1/folder1_1/filename2.txt'
    
    // Create a file in a subfolder with some content
    file ('folder1/folder1_1/filename3.txt') {
        "I am some content"
    }
    
    // Create a file in a subfolder with a multiline content
    file ('folder1/folder1_1/filename4.txt') {
        """I am some content
Split over
multiple lines"""
    }
    
    // Create a directory
    path 'folder2/folder2_1'
}
```

### 3. Run the Task

Execute the `createLayout` task to generate the structure:

```bash
./gradlew createLayout
```

## Tasks

The plugin exposes a single task `createLayout` which creates the specified layout. If a file exists it will not overwrite it.