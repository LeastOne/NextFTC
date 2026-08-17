# Repository Instructions

## Design and Kotlin style

- Favor the smallest straightforward implementation that remains readable to student programmers.
- Prefer concise, idiomatic Kotlin over Java-style ceremony.
- Introduce encapsulation, indirection, lazy evaluation, naming ceremony, or new abstractions only when they solve a demonstrated problem.
- Do not add visibility modifiers merely by habit. Use restricted visibility when it prevents a concrete misuse or clarifies an important boundary.
- Prefer direct imports of frequently used singleton members, such as `gamepad2`, over repeated qualified references.
- Keep Panels-adjustable configuration as plainly visible mutable subsystem properties. Do not turn those values into constants merely to silence IDE suggestions.
- Mark Panels-configurable singleton objects with `@Configurable`; their mutable Kotlin properties are discoverable without `@JvmField`.
- Mark mutable implementation state in a configurable object with `@field:IgnoreConfigurable` so Panels exposes only intentional settings.

## Subsystems and commands

- Implement robot subsystems as Kotlin singleton `object`s under `org.firstinspires.ftc.teamcode.subsystems`; shared OpModes discover them automatically, so do not maintain a manual registration list.
- Keep controls, commands, hardware behavior, and relevant state together in the owning subsystem when practical; do not create separate control or command classes without a demonstrated need.
- Keep NextBindings definitions with the subsystem they control.
- Declare annotated configuration settings in the `Config` primary constructor; their source order defines their menu and telemetry order.
- Give exposed commands short verb or state names such as `open`, `close`, `up`, and `down`. Do not add a redundant `Command` suffix.
- Use the project's delegated `instant` adaptation when declaring reusable instant commands so their subsystem-qualified names and execution logs are inferred automatically.
- Do not manually name or log every command when the adaptation can derive that information.
- Implement subsystem hardware with the project's `Hardware` wrappers. The adapted `SubsystemComponent` discovers and initializes them automatically, disabling only the subsystem containing failed hardware.
- Put reusable NextFTC integration code under the appropriate concern-specific `adaptations.nextftc` subpackage, such as `commands`, `hardware`, `logging`, or `subsystems`.

## OpModes

- Derive robot OpModes from the project's shared `OpMode` base so logging, bulk reads, and subsystem lifecycle behavior remain consistent.
- Name the general driver-controlled OpMode `Teleop`.
- Use `@TeleOp` without an explicit name when the class name already supplies the intended Driver Station name.
- Prefer a clear class name over aliasing a conflicting import.

## Logging

- Use `tel` for current state and `log` for historical events; never route a telemetry call into RobotLog or a log call into regular `addData()` telemetry.
- Use `VERBOSE` for unusually detailed state or events, `DEBUG` for diagnostic information, and `INFO` for normal match-useful state and significant events.
- Include the level, source, and caption/message in Driver Station output using the project's pipe-delimited format.
- Use `hardware.update { ... }` when changing hardware and reporting it in the same cycle; use `hardware.tel()` when only reporting it.
- Let hardware telemetry infer its source from the configured hardware name and retain raw numeric values for Panels graphing.
- Preserve every `log` event in RobotLog independently of configurable Driver Station level and filter settings.
- Rebuild regular Driver Station telemetry as a current snapshot each cycle and retain event history only in its telemetry log.
- Keep the titled `CONFIG`, `TEL`, and `LOG` sections visually and conceptually distinct.

## TeamCode test coverage

- Every change to executable code under `TeamCode/src/main` must add or update meaningful unit tests for the affected behavior.
- Maintain 100% line and branch coverage for TeamCode, as enforced by `:TeamCode:verifyUnitTestCoverage`.
- Do not weaken coverage thresholds or add exclusions merely to make coverage pass. Exclusions are appropriate only for generated code with no independently testable project behavior.
- Tests must assert observable behavior; executing lines without meaningful assertions is not sufficient.
- Keep each subsystem's tests in the corresponding subsystem package, with one test class/file per subsystem.
- Keep the subsystem test harness generic. It may provide hardware mocks by hardware type and configured name, but must not contain knowledge of specific subsystem devices.
- Before completing a TeamCode change, run `./gradlew :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug` (use `gradlew.bat` on Windows) and report any failures or intentionally untested hardware-only behavior.

## Git history

- Use `3drdProgramming <programming@3droboticsduluth.com>` as the repository Git identity.
- Maintain clean, organized commits as work proceeds and push verified completed work without waiting for a separate request.
- Organize affected history sensibly by default, including folding fixups into their logical commits and rebasing when useful.
- When asked to organize commits, keep tests in the same logical commit as the behavior they test, fold fixups into the commit they refine, and avoid miscellaneous cleanup commits when a clearer logical placement exists.
- When rewritten published history must be pushed, use `--force-with-lease`, never an unconditional force push.
