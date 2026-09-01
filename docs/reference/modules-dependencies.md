# Modules and Dependencies

## Module Ownership

| Module | Kind | Owns | Must not own |
|---|---|---|---|
| `FtcRobotController` | Android app/library host from FIRST | FTC application, SDK integration, samples/resources | 3DRD robot policy |
| `3drdNextFTC` | Android library | Reusable 3DRD NextFTC lifecycle, commands, hardware, config, diagnostics, Pedro helpers | TeamCode imports, hardware-map names, season geometry |
| `3drdQuanomous` | Android library | Generic Quanomous parsing, storage, options, compilation | NextFTC command types, season step vocabulary |
| `TeamCode` | Android application | OpModes, robot constants, game model, hardware telemetry policy, mechanisms, controls, vision, strategy | Cross-team reusable fixes that belong in a library |

## Build Versions

These are the versions required to reproduce the documented tree:

| Component | Version |
|---|---:|
| FTC SDK artifacts | 12.0.0 |
| Gradle wrapper | 9.1.0 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin Android / reflect | 2.4.20 |
| Gradle daemon toolchain | Java 17 (vendor-neutral) |
| Android compile SDK (owned modules) | 34 |
| Android minimum SDK | 24 |
| Android target SDK | 28 |
| Java/Kotlin bytecode target | 1.8 |
| NextFTC FTC | 1.1.0 |
| NextFTC hardware | 1.1.0 |
| NextFTC bindings | 1.0.1 |
| Next Control | 1.0.0 |
| NextFTC Pedro extension | 1.0.0 |
| Pedro Pathing FTC | 2.1.2 |
| FullPanels API | 1.0.13 |
| Sloth-compatible FullPanels runtime | 0.3.2+1.0.13 |
| Dairy Sloth runtime / Load plugin | 0.3.2 |
| AndroidX AppCompat | 1.2.0 |
| JUnit | 4.13.2 |
| Mockito | 5.24.0 |

The FTC dependency set also includes version 12.0.0 of Inspection, Blocks,
RobotCore, RobotServer, OnBotJava, Hardware, FtcCommon, and Vision.

## Artifact Repositories

- Maven Central
- Google Maven
- `https://mymaven.bylazar.com/releases`
- `https://maven.pedropathing.com/`
- `https://repo.dairy.foundation/releases`

No IDE plugin installation should be required to resolve runtime libraries. Sloth's
Load integration is a Gradle plugin committed with the project, not an Android
Studio plugin. TeamCode substitutes the Sloth-compatible FullPanels runtime while
the reusable library continues compiling against the standard FullPanels API.

## Why Versions Are Pinned

FTC robot builds combine Android, Gradle, Kotlin, device SDK, command framework,
pathing, and dashboard code. Advancing one can change binary compatibility or API
shape. Upgrade versions as a deliberate conceptual change with full verification
and robot smoke tests, not as incidental cleanup.

## Coverage Configuration

All three owned modules apply JaCoCo and require a ratio of `1.0` for line and branch
counters. Generated Android classes, build configuration, Kotlin default-interface
artifacts, and test classes are excluded. Reports are generated under each module's
build reports directory by `unitTestCoverage`.

`3drdNextFTC` includes execution evidence from its own tests and the real TeamCode
consumer suite. This allows generic integration behavior to be proven through
actual subsystem use without moving robot policy into the library.
