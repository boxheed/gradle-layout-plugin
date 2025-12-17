# Tasks: Gradle Layout Plugin

**Input**: Design documents from `/workspace/specs/001-gradle-layout-plugin/`

## Phase 1: Setup (Shared Infrastructure)

- [X] T001 [P] Create the basic directory structure `src/main/groovy`, `src/test/groovy`, `src/main/resources`.
- [X] T002 [P] Create the `build.gradle` file for the plugin project, including the `groovy` plugin and dependencies on `gradleApi()`.
- [X] T003 [P] Create the plugin properties file in `src/main/resources/META-INF/gradle-plugins/com.fizzpod.layout.properties`.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [X] T004 Create the main plugin class `LayoutPlugin.groovy` in `src/main/groovy/com/fizzpod/gradle/plugins/layout/`.
- [X] T005 Create the plugin extension class `LayoutPluginExtension.groovy` in `src/main/groovy/com/fizzpod/gradle/plugins/layout/`.
- [X] T006 Create the `LayoutFile.groovy` class in `src/main/groovy/com/fizzpod/gradle/plugins/layout/`.
- [X] T007 Create the `LayoutPath.groovy` class in `src/main/groovy/com/fizzpod/gradle/plugins/layout/`.
- [X] T008 Implement the basic `apply` method in `LayoutPlugin.groovy` to register the `layout` extension.

---

## Phase 3: User Story 1 - Define a basic project structure (Priority: P1) 🎯 MVP

**Goal**: As a developer, I want to define a set of empty files and directories in my `build.gradle` so that I can quickly scaffold a new project's structure.

**Independent Test**: A `build.gradle` file with a `layout` block defining files and paths can be used to generate the specified structure in an empty directory.

### Tests for User Story 1
- [X] T009 [US1] Create the test specification `LayoutPluginSpec.groovy` in `src/test/groovy/com/fizzpod/gradle/plugins/layout/`.
- [X] T010 [US1] Write a test in `LayoutPluginSpec.groovy` that defines a layout with an empty file and verifies that the `createLayout` task creates it.
- [X] T011 [US1] Write a test in `LayoutPluginSpec.groovy` that defines a layout with a directory and verifies that the `createLayout` task creates it.
- [X] T012 [US1] Write a test in `LayoutPluginSpec.groovy` that checks if an existing file is not overwritten.

### Implementation for User Story 1

- [X] T013 [US1] Implement the `file(String path)` method in `LayoutPluginExtension.groovy` to add an empty file to the layout.
- [X] T014 [US1] Implement the `path(String path)` method in `LayoutPluginExtension.groovy` to add a directory to the layout.
- [X] T015 [US1] Implement the `createLayout` task in `LayoutPlugin.groovy` to iterate over the defined paths and files and create them.

---

## Phase 4: User Story 2 - Define files with content (Priority: P2)

**Goal**: As a developer, I want to specify the initial content of files within my `build.gradle` `layout` block, so that I can create boilerplate files with default content.

**Independent Test**: A `build.gradle` file with a `layout` block defining a file with content can be used to generate that file with the correct content.

### Tests for User Story 2

- [X] T016 [US2] Write a test in `LayoutPluginSpec.groovy` that defines a layout with a file containing single-line content and verifies its creation.
- [X] T017 [US2] Write a test in `LayoutPluginSpec.groovy` that defines a layout with a file containing multi-line content and verifies its creation.

### Implementation for User Story 2

- [X] T018 [US2] Implement the `file(String path, Closure content)` method in `LayoutPluginExtension.groovy` to add a file with content to the layout.
- [X] T019 [US2] Update the `createLayout` task in `LayoutPlugin.groovy` to handle writing content to files.

---

## Phase 5: Polish & Cross-Cutting Concerns

- [X] T020 [P] Update the `README.md` file with comprehensive documentation.
- [X] T021 [P] Create example projects using the plugin.
- [X] T022 [P] Review code for quality and adherence to conventions.

---

## Dependencies & Execution Order

- **Phase 1 & 2**: Must be completed before any user story work.
- **User Story 1**: Depends on Phase 1 & 2.
- **User Story 2**: Depends on User Story 1.

---
