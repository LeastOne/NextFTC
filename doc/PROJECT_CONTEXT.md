# NextFTC Project Context

This document gives collaborators and AI assistants the architectural and historical context that cannot be inferred reliably from source code alone. The repository is the authority for current implementation, tests, and dependencies. [`AGENTS.md`](../AGENTS.md) is the authority for working conventions. If this document conflicts with either, inspect recent history and resolve the discrepancy rather than assuming this snapshot is newer.

## Purpose

This repository explores a Kotlin- and NextFTC-based foundation for 3D Robotics Duluth FTC robots. It began with the current official FTC Robot Controller release and initially used the prior-season Osiris robot as a practical migration and hardware-validation target.

The goal is not merely to rewrite the older robot code in Kotlin. The goal is to retain useful behavior while finding a clearer, smaller, more composable NextFTC implementation that student programmers can understand and extend. Concision matters, but recognizable local patterns and student readability matter more than demonstrating every available Kotlin idiom.

The project is also separating stable, reusable infrastructure from season-specific robot code so multiple 3D Robotics Duluth teams and future seasons can share a common foundation. The reusable modules may eventually be published as public Maven artifacts, but publication and release automation have not yet been established.

## Repositories and reference implementations

- `LeastOne/NextFTC` is the current implementation and source of truth.
- `LeastOne/Decode` and the team branches in the older Decode repository are behavioral references for the 2025 Decode robots. The local reference checkout has also included the `leastone/features/logging-ideas` and `leastone/features/logging-refactor` branches.
- `FIRST-Tech-Challenge/FtcRobotController` is the upstream FTC foundation and is configured as the `ftc` remote.
- NextFTC, Panels, Pedro Pathing, and their official documentation are upstream integration references.

When consulting the old repository, distinguish intentional robot behavior from supporting ceremony accumulated around the previous SolversLib architecture. Preserve observable behavior unless a change is deliberate, but express it using the current NextFTC model instead of mechanically recreating old command, control, and subsystem class layers.

Third-party implementations should remain recognizable. The goBILDA Prism driver, for example, is vendor Java source carried into TeamCode rather than translated into project-owned Kotlin.

## Current architecture

The Gradle project contains four modules.

### `FtcRobotController`

This is the official FTC Robot Controller application foundation. Changes near this layer should remain narrowly focused on SDK compatibility, build configuration, warning handling, and similarly foundational concerns so future FTC upgrades remain understandable.

### `3drdNextFTC`

This Android library contains season-neutral infrastructure intended for reuse across robots and teams under the `org.firstinspires.ftc.threedrd` namespace. Its responsibilities currently include:

- NextFTC command composition, inferred command naming, instant commands, deferred commands, and repetition.
- NextBindings lifecycle management.
- Config setting discovery, menu metadata, diagnostics binding, persistence, and storage.
- Hardware wrappers and generic hardware-update mechanics.
- Logging and telemetry infrastructure.
- Shared OpMode state helpers.
- Subsystem discovery, lifecycle, hardware initialization, fault isolation, and shutdown.
- Reusable drive and navigation subsystem foundations.
- Pedro Pathing driver control, pose and distance helpers, path progress units, and field drawing.
- Small FTC- or platform-level utilities such as debounce.

This library must not depend on TeamCode or contain Decode-specific poses, hardware names, match strategy, or presentation policy likely to differ among teams.

### `3drdQuanomous`

This Android library owns reusable Quanomous parsing, compilation, and storage concepts. It is intentionally independent of `3drdNextFTC`. Quanomous is useful beyond this specific NextFTC adaptation, so any integration between the two libraries belongs in the consuming robot application or a future explicit integration layer.

### `TeamCode`

This module contains the actual robot and season implementation:

- Teleop and autonomous OpModes.
- Decode game concepts such as alliance and side.
- Robot configuration and Pedro constants.
- Osiris hardware mappings and hardware telemetry presentation.
- Drive and navigation specializations.
- Auto, Quanomous, vision, intake, conveyor, flywheel, gate, deflector, kickstand, lights, and timing subsystems.
- The vendor goBILDA Prism implementation.

Hardware telemetry formatting deliberately remains here. Generic wrappers can invoke configurable telemetry hooks, but each team may reasonably choose different captions, precision, levels, and displayed values.

## Shared OpMode and lifecycle model

Robot OpModes derive from the shared TeamCode `OpMode`, which composes the common components for telemetry, bindings, bulk reads, Pedro, field drawing, configuration, and subsystem discovery.

Subsystems are normally Kotlin singleton `object`s. The subsystem component discovers implementations reflectively instead of maintaining a central manual list. Reflection is acceptable here because discovery happens at a lifecycle boundary and removes a recurring registration hotspot. Reflective discovery and configuration metadata should be resolved or cached; repeated classpath or field scanning does not belong in periodic robot loops.

The subsystem lifecycle is designed around these behaviors:

- Hardware wrappers initialize automatically.
- A hardware initialization failure disables the affected subsystem rather than necessarily preventing unrelated mechanisms from operating.
- Configuration controls are available during initialization in both Teleop and Auto.
- Robot-driving controls are registered only when Teleop starts.
- Reinitializing or switching OpModes must not accumulate duplicate bindings.
- Default commands are scheduled through NextFTC when their subsystem is otherwise idle.
- `stop()` performs immediate, minimal, idempotent powered-hardware cleanup. Shutdown must not depend on another periodic call occurring.
- Subsystems are stopped in reverse lifecycle order, and one subsystem's shutdown failure should not prevent the others from stopping.

These lifecycle rules were informed by hardware testing that exposed both premature Teleop controls during initialization and duplicate configuration bindings after repeated OpMode initialization.

## Commands and composition

Commands generally live with the subsystem whose behavior they express. Separate command and control classes are avoided unless they provide a demonstrated benefit.

Reusable instant commands use the project adaptation so command names and execution diagnostics can be inferred from the owning subsystem and property. Runtime-dependent reusable commands use delegated deferred construction. Functions remain appropriate when a caller supplies parameters.

Common command names should retain their subsystem qualifier at call sites when an unqualified import would hide meaning. `Gate.open`, for example, is clearer inside an autonomous composition than a statically imported `open`, because many mechanisms can have open and close operations.

Autonomous routines favor composable command chains. Named navigation operations such as `Drive.toScore` encapsulate path construction rather than exposing a long sequence of direct drive-to-pose calls. Navigation overloads use meaningful distance, angle, path-progress, and T-progress units rather than ambiguous raw doubles when a suitable unit exists.

Driver control is a subsystem default command, allowing autonomous-assist commands to interrupt it using normal scheduler requirements and allowing it to resume naturally afterward.

## Configuration and Panels

The TeamCode `Config` subsystem contains a nested configurable data class near the top of the file. This is the primary place students add match and robot settings. `@Setting` metadata drives the Driver Station menu, and source declaration order drives menu and telemetry order; alliance, side, and Quanomous therefore remain near the top because they are required autonomous selections.

Implementation state such as the selected menu index lives outside the persisted data class. Runtime-only properties use `@Transient` where appropriate. Panels configuration remains useful for debugging, but match behavior should not become complicated merely to persist every Panels edit.

Persistence is change-driven and debounced. A menu value change marks configuration dirty, and serialization occurs only after the debounce interval rather than on every periodic iteration.

On autonomous initialization, alliance and side reset to `UNKNOWN` and Quanomous resets to no selection. The configuration display warns about missing required selections, and Auto fails clearly if started without all three. There is no fallback sample routine for a missing or unloadable Quanomous program because that condition is operationally critical.

Changing alliance or side resets the configured starting pose while the robot is still being placed. Starting Auto must not overwrite later pose changes. This preserves the prior robot workflow: drivers select the known start, and Pedro may then track a deliberate physical repositioning before the match begins.

## Logging and telemetry

Logging and telemetry are separate concepts even though both can appear on the Driver Station.

- `tel` represents current state. Regular telemetry is rebuilt as a current snapshot and is not copied into RobotLog.
- `log` represents historical events. Every log event is written to RobotLog for Logcat independently of whether current Driver Station filters display it.
- The Driver Station has visually distinct `CONFIG`, `TEL`, and `LOG` sections.
- Output uses consistent level indicators and pipe-delimited source, caption, message, and value fields.
- Driver Station log history can be rebuilt when its level or filter changes so stale or newly matching entries behave predictably.
- Command diagnostics report meaningful scheduler-state changes rather than logging the same snapshot every loop.

Telemetry and logging each retain their own Panels-adjustable level and filter. The Config data class exposes a common `level` and `filter` convention that diagnostics can bind reflectively when those fields exist. This avoids requiring every team's Config type to inherit a diagnostics base class. Missing fields safely default diagnostics to off.

Hardware telemetry infers its source from the configured hardware name and uses team-selected levels and formatting. Hardware updates and telemetry can be paired through `hardware.update { ... }`; read-only reporting uses `hardware.tel()`.

## Drive, navigation, and Pedro Pathing

The reusable drive and navigation foundations live in `3drdNextFTC`; robot-specific controls, speed choices, paths, poses, tuning, and match behavior remain in TeamCode.

Pedro constants intentionally follow Pedro's documented structure closely. Students and mentors will compare this file with upstream tuning documentation, so novel wrappers here have a higher clarity cost than they would elsewhere. Robot width and length are treated as natural robot specifications in the Pedro constants and are used by field drawing.

The project supports robot-centric and field-centric Teleop control. Earlier hardware testing found swapped axial/lateral axes, an inverted turn input, and a field-centric heading integration issue; those behaviors were corrected and retested on Osiris.

Navigation transformations expose readable alliance and side operations and use angle units rather than repeated manual radian conversion. Pedro's follower pose is the runtime localization authority. Navigation state may still track construction context where needed to compose sequential paths, but it should not become a competing localization source.

Field drawing uses the prior robot's readable outline style rather than an invisible zero-width stroke. The coordinate transform must respect the FTC field orientation used for Decode rather than assuming the screen's ordinary Cartesian orientation.

Pedro tuning OpModes are intentionally not copied into the main project at present. A possible future approach is a dedicated `pedro-tuning` branch tracking Pedro's tuning repository, but that work has been deferred. The NextFTC Pedro extension and Pedro core versions should remain compatible; upgrading one independently requires verification rather than assuming the newest individual artifact is safe.

## Control systems

Next Control is included in the reusable base. Software position and heading corrections in Drive use Next Control rather than repurposing Pedro's internal path-following controllers for unrelated subsystem control.

Embedded motor velocity control remains appropriate where the hub's fast, deterministic control loop is beneficial, such as flywheel velocity. Moving such a loop into application software should require a concrete reason and hardware validation rather than consistency alone.

## Testing and hardware validation

The repository enforces meaningful unit coverage for executable code in TeamCode and both reusable libraries. Tests for subsystem behavior live with the corresponding subsystem package and use a generic hardware test harness that resolves mocks by hardware type and configured name. The harness must not accumulate knowledge of particular robot devices.

The intended pre-handoff verification command is documented in `AGENTS.md` and includes checks, coverage verification, and a TeamCode debug build.

Hardware validation reported during development has included:

- Successful Teleop initialization and start on Osiris.
- Deflector and gate operation.
- Driver Station and Logcat telemetry/logging behavior and filtering.
- Configuration menu interaction during initialization.
- Repeated OpMode initialization after binding-lifecycle fixes.
- Auto required-setting validation.
- Robot-centric and field-centric driving after axis, turn, and heading fixes.
- Panels field visualization after coordinate and drawing adjustments.

This does not mean every migrated Decode mechanism or full autonomous strategy has completed integrated match testing. Unit coverage cannot replace final validation on the real robot, especially for motor directions, sensor orientation, camera behavior, tuning, and mechanism limits.

## Git history model

History is intentionally curated as a readable build-up rather than a chronological diary of every experiment:

1. Start from the official FTC repository and apply foundational compatibility fixes and utilities.
2. Add Kotlin, NextFTC, Panels, Pedro, Next Control, and other base dependencies and adaptations.
3. Establish reusable libraries and the minimal functional robot foundation: shared OpModes, lifecycle, configuration, drive, and navigation.
4. Add season- and robot-specific mechanisms, strategy, vision, and autonomous behavior.

Tests should be folded into the commits that introduce the behavior they test. Small corrections should normally be fixed up into their logical commit. Published rewrites use `--force-with-lease`. A larger milestone reorganization has been considered but was intentionally deferred pending mentor alignment.

## Open decisions and next priorities

The following subjects have been discussed but are not fully settled or completed:

- Decide the final repository and release structure for `3drdNextFTC` and `3drdQuanomous`, including whether they remain modules here or move to dedicated public repositories.
- Establish GitHub Actions build, coverage, packaging, and release workflows.
- Select and configure a public Maven publication target and artifact coordinates for the reusable libraries.
- Decide whether and when to maintain a dedicated Pedro tuning branch.
- Continue integrated hardware and autonomous testing on Osiris.
- Continue reviewing migrated mechanisms for opportunities to use a clearer NextFTC-native pattern without changing established robot behavior.
- Revisit debounce only if actual behavior demonstrates a better NextFTC alternative is needed.
- Revisit dependency versions as a coordinated compatibility exercise, particularly around NextFTC's Pedro extension and Pedro core.

Avoid reopening settled decisions merely because another implementation is possible. Reconsider them when new evidence, upstream capabilities, hardware behavior, or cross-team reuse requirements change the tradeoff.

## Glossary

- **3DRD / 3D Robotics Duluth**: The organization supporting multiple FTC teams; reusable code uses the legal Java/Kotlin namespace segment `threedrd`.
- **Osiris**: The prior-season robot used as the first practical target for the NextFTC migration.
- **Decode repository**: The older Java/SolversLib implementation used for behavioral comparison and porting inspiration.
- **NextFTC**: The command-based Kotlin-oriented framework now used for robot lifecycle, commands, bindings, and hardware integration.
- **Panels**: The web dashboard used for configurable values, telemetry, graphing, and Pedro field visualization during debugging.
- **Pedro Pathing**: The localization and path-following library used for drive and autonomous navigation.
- **Quanomous**: The project's data-driven autonomous program format and compiler.
- **Adaptation**: Project-owned integration code that fills a concrete gap between FTC, NextFTC, Pedro, Panels, or team conventions. Stable cross-team adaptations belong in a reusable library; robot policy remains in TeamCode.

## Context version

- Last reviewed: 2026-08-26
- Repository: `LeastOne/NextFTC`
- Branch: `main`
- Based on commit: `e8f4037f7fc4187704dd2fdf0bb020336eb4c40c`

