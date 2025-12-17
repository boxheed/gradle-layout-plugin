# Research for Gradle Layout Plugin

## Decisions

- **Language**: Java 17 and Groovy
- **Dependencies**: Core Gradle API
- **Testing**: Spock

## Rationale

The user specified the language and dependency constraints. Spock is a standard and powerful testing framework for Groovy and Java projects, and it is already used in the `buildSrc` part of this project.

## Alternatives Considered

- **Testing**: JUnit could have been used, but Spock is more expressive for this project's context.
