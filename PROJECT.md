# Project rules and policies

## Git policy

- Default and release branch is **master**.
- RC (release candidate) branch is **dev**.
- Use [Conventional Commits](https://www.conventionalcommits.org) specification for writing commit messages in a structured and consistent way.

## Project scheme
```text
/
├── apps/
│   ├── compose/               # KMP app module (entry point for front end app)
│   │   ├── src/                     
│   │   │   ├── commonMain/    # Shared code across platforms
│   │   │   ├── androidMain/   # Android-specific code
│   │   │   └── desktopMain/   # Desktop-specific code (JVM) (In progress)
│   │   └── build.gradle.kts   # KMP app module build configuration
│
├── features/                     # KMP project features
│   ├── chat/                     # KMP chat module
│   └── utils/                    # KMP utilities module
│
├── gradle/                       # Gradle wrapper
│   └── wrapper/
│   │   └── gradle-wrapper.properties # Gradle wrapper properties
│   └── libs.versions.toml        # KMP project dependencies and properties
│
├── build.gradle.kts              # KMP project build configuration
├── gradle.properties             # KMP project gradle properties
├── gradlew                       # KMP project gradle wrapper script (for *nix)
├── gradlew.bat                   # KMP project gradle wrapper script (for Windows)
└── settings.gradle.kts           # KMP project settings
```
