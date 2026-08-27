# Repository Instructions

Read `doc/PROJECT_CONTEXT.md` for the project's purpose, architectural rationale, reference repositories, validation status, and open decisions. This file remains authoritative for working conventions when the two overlap.

## Design and Kotlin style

- Favor the smallest straightforward implementation that remains readable to student programmers.
- Prefer concise, idiomatic Kotlin over Java-style ceremony.
- Prefer a small set of recognizable local precedents over showcasing every valid Kotlin form. Format neighboring declarations that serve the same role consistently, even when one could be made marginally shorter.
- Keep simple expression-bodied functions, properties, short argument lists, and trivial command lambdas on one line. Expand them when the logic has meaningful structure, then use a consistent multiline shape across analogous declarations.
- A short lambda containing only a few obvious assignments may remain on one line with semicolons; use a multiline body once branching, sequencing, or side effects would become easy to miss.
- Use blank lines to separate conceptual groups and phases, such as hardware, state, commands, lifecycle methods, calculations, and telemetry. Avoid blank lines that fragment a single logical operation.
- Prefer direct delegated hardware declarations and direct expressions over one-use intermediary properties or local aliases, unless the intermediary provides meaningful naming, snapshot semantics, reuse, or readability.
- Introduce encapsulation, indirection, lazy evaluation, naming ceremony, or new abstractions only when they solve a demonstrated problem.
- Do not add visibility modifiers merely by habit. Use restricted visibility when it prevents a concrete misuse or clarifies an important boundary.
- Prefer direct imports of frequently used singleton members, such as `gamepad2`, over repeated qualified references.
- Keep Panels-adjustable configuration as plainly visible mutable subsystem properties. Do not turn those values into constants merely to silence IDE suggestions.
- Mark Panels-configurable singleton objects with `@Configurable`; their mutable Kotlin properties are discoverable without `@JvmField`.
- Do not add `@field:IgnoreConfigurable` to implementation state preemptively. Use it only when Panels exposure causes a demonstrated technical or usability problem.

## Architecture and ownership

- Keep reusable, season-neutral NextFTC and Pedro adaptations in `3drdNextFTC` under the `org.firstinspires.ftc.threedrd` namespace. This includes common hardware wrappers and updates, command composition, subsystem lifecycle, configuration infrastructure, logging, telemetry, and the reusable drive/navigation foundations.
- Reusable library modules must not depend on `TeamCode` or contain season-specific types, constants, poses, hardware names, or behavior.
- Keep `3drdQuanomous` independently reusable. It must not depend on `3drdNextFTC`; integrations between the two belong in the consuming robot project or another explicit integration layer.
- Keep season-, game-, robot-, and team-policy-specific behavior in `TeamCode`. In particular, hardware telemetry presentation belongs in `TeamCode` because teams may choose different captions, levels, precision, and displayed values, while generic hardware update mechanics belong in `3drdNextFTC`.
- Before adding code to `TeamCode/adaptations`, decide whether it is genuinely robot-specific. Prefer the reusable library for stable cross-team behavior, but do not force policy or likely customization points into the library merely to reduce TeamCode size.
- Preserve third-party source code in its supplied language and recognizable form when practical. Do not translate vendor code such as the goBILDA Prism driver into Kotlin or substantially restyle it unless maintaining a deliberate fork.
- Keep integration surfaces that students will compare with upstream documentation—especially Pedro Pathing constants and tuning setup—close to the documented upstream shape. Avoid clever wrappers there unless they solve a concrete project requirement.
- When porting from the older `Decode` repository, preserve observable robot behavior unless a behavior change is intentional and documented. Convert the implementation to the NextFTC model rather than mechanically carrying forward every helper or abstraction.
- Reflection is acceptable for one-time discovery, configuration metadata, or lifecycle setup when it removes a maintenance hotspot. Resolve and cache reflective metadata outside periodic loops; do not repeatedly scan classes or fields on the robot's hot path.

## Subsystems and commands

- Implement robot subsystems as Kotlin singleton `object`s under `org.firstinspires.ftc.teamcode.subsystems`; shared OpModes discover them automatically, so do not maintain a manual registration list.
- Keep controls, commands, hardware behavior, and relevant state together in the owning subsystem when practical; do not create separate control or command classes without a demonstrated need.
- Keep NextBindings definitions with the subsystem they control.
- Declare annotated configuration settings in the `Config` primary constructor; their source order defines their menu and telemetry order.
- Keep the robot's configurable data class near the top of the Config subsystem so students have one obvious place to add settings. Keep menu settings there and implementation/menu state outside it; use `@Transient` for runtime-only values that should not be persisted.
- Persist configuration only after an actual menu value change and debounce storage writes. Do not serialize configuration every periodic cycle, and do not complicate match behavior merely to persist changes made through Panels.
- Give exposed commands short verb or state names such as `open`, `close`, `up`, and `down`. Do not add a redundant `Command` suffix.
- Use the project's delegated `instant` adaptation when declaring reusable instant commands so their subsystem-qualified names and execution logs are inferred automatically.
- Use delegated `deferred` commands when reusable command construction depends on live runtime state; use functions when callers must supply parameters. Keep analogous command declarations consistent so students can infer the pattern.
- Do not manually name or log every command when the adaptation can derive that information.
- Implement subsystem hardware with the project's `Hardware` wrappers. The adapted `SubsystemComponent` discovers and initializes them automatically, disabling only the subsystem containing failed hardware.
- Use a subsystem's lifecycle `stop()` for immediate, minimal, idempotent cleanup. Directly stop powered hardware there rather than relying on another `periodic()` call; keep command-level `stop` behavior distinct for normal operation.
- Organize reusable NextFTC integration code in `3drdNextFTC` by concern, such as `commands`, `hardware`, `logging`, `subsystems`, or `telemetry`; reserve TeamCode adaptations for robot-specific integration and customization.
- Qualify commands with common names through their owning subsystem—for example, `Gate.open`—when static imports would obscure which mechanism acts. Static imports remain appropriate for unambiguous project-wide vocabulary.
- Register subsystem controls once per OpMode lifecycle. Configuration controls must remain usable during initialization, while robot-driving controls must not become active until a Teleop starts; stopping or reinitializing an OpMode must not accumulate duplicate bindings.
- Express navigation distances, angles, and path progress with the project's unit types and overloads rather than ambiguous raw `Double` parameters when a suitable unit exists.
- Implement continuously available behavior, such as driver control, as a NextFTC subsystem default command so command requirements and interruptions remain scheduler-managed.

## OpModes

- Derive robot OpModes from the project's shared `OpMode` base so logging, bulk reads, and subsystem lifecycle behavior remain consistent.
- Name the general driver-controlled OpMode `Teleop`.
- Use `@TeleOp` without an explicit name when the class name already supplies the intended Driver Station name.
- Prefer a clear class name over aliasing a conflicting import.
- Autonomous initialization must preserve pose adjustments made after selecting the starting configuration. Do not silently reset the follower pose when Auto starts.
- Treat required autonomous selections as required input: reset the applicable selections on Auto initialization, show their absence during initialization, and fail clearly rather than starting a fallback routine.

## Logging

- Use `tel` for current state and `log` for historical events; never route a telemetry call into RobotLog or a log call into regular `addData()` telemetry.
- Use `VERBOSE` for unusually detailed state or events, `DEBUG` for diagnostic information, and `INFO` for normal match-useful state and significant events.
- Include the level, source, and caption/message in Driver Station output using the project's pipe-delimited format.
- Use `hardware.update { ... }` when changing hardware and reporting it in the same cycle; use `hardware.tel()` when only reporting it.
- Let hardware telemetry infer its source from the configured hardware name and retain raw numeric values for Panels graphing.
- Preserve every `log` event in RobotLog independently of configurable Driver Station level and filter settings.
- Rebuild regular Driver Station telemetry as a current snapshot each cycle and retain event history only in its telemetry log.
- Keep the titled `CONFIG`, `TEL`, and `LOG` sections visually and conceptually distinct.

## Robot and library test coverage

- Every change to executable code under `TeamCode/src/main`, `3drdNextFTC/src/main`, or `3drdQuanomous/src/main` must add or update meaningful unit tests for the affected behavior.
- Maintain 100% line and branch coverage for TeamCode and both reusable library modules.
- Do not weaken coverage thresholds or add exclusions merely to make coverage pass. Exclusions are appropriate only for generated code with no independently testable project behavior.
- Tests must assert observable behavior; executing lines without meaningful assertions is not sufficient.
- Keep each subsystem's tests in the corresponding subsystem package, with one test class/file per subsystem.
- Keep the subsystem test harness generic. It may provide hardware mocks by hardware type and configured name, but must not contain knowledge of specific subsystem devices.
- Before completing robot or library changes, run `./gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :3drdQuanomous:check :3drdQuanomous:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug` (use `gradlew.bat` on Windows) and report any failures or intentionally untested hardware-only behavior.

## Git history

- Use `3drdProgramming <programming@3droboticsduluth.com>` as the repository Git identity.
- Maintain clean, organized commits as work proceeds and push verified completed work without waiting for a separate request.
- Organize affected history sensibly by default, including folding fixups into their logical commits and rebasing when useful.
- When asked to organize commits, keep tests in the same logical commit as the behavior they test, fold fixups into the commit they refine, and avoid miscellaneous cleanup commits when a clearer logical placement exists.
- When rewritten published history must be pushed, use `--force-with-lease`, never an unconditional force push.
- Preserve the repository's conceptual build-up when placing or rewriting commits: start with the upstream FTC release and foundational fixes; add NextFTC and other base dependencies; add reusable libraries and the minimal functional robot foundation (OpModes, drive, and navigation); then add season- and robot-specific features.
- Place a change in the earliest logical commit whose behavior it completes, but do not blur these architectural milestones merely to minimize commit count.
