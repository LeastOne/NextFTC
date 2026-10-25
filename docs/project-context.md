# Project context

This page is the concise handoff for mentors, contributors, and coding agents. The
linked pages are canonical when more detail is needed.

## Purpose

The repository begins with FTC Robot Controller v11.2, inherits the reusable 3DRD
Kotlin/NextFTC platform and neutral TeamCode scaffold from Quickstart develop,
then adds the Decode/Osiris robot. The platform adopts FTC SDK 12 and Pedro 2.1.2
while retaining NextFTC v1; Pedro 3 and NextFTC v2 remain future evaluations.
Current `main` is not a neutral season template.

This personal branch also explores Kotlin and NextFTC approaches. Flywheel now
uses independent NextControl software velocity systems rather than the deployed
Hub PIDF loop. Its example gains and revised readiness contract are an exploration,
not a claim of equivalent robot behavior; see the [mechanism specification](requirements/osiris-mechanisms.md#flywheel).

## Read First

- [Get started](index.md)
- [Our robot templates](robot/index.md)
- [Choose drivetrain and localization](guides/drivetrain.md)
- [Document your robot](guides/document-your-robot.md)
- [Start a new season](guides/new-season.md)
- [Why Kotlin?](guides/why-kotlin.md)
- [Build the first robot](guides/first-robot.md)
- [Architecture overview](architecture/overview.md)
- [Requirements](requirements/index.md)
- [Rebuild guide](rebuild/index.md)
- [Audit a one-shot rebuild](rebuild/one-shot.md)
- [Architectural decisions](reference/decisions.md)

## Non-Negotiable Boundaries

- Reusable NextFTC behavior belongs in `3drdNextFTC`.
- Quanomous parsing/storage/compiler belongs in independent `3drdQuanomous`.
- Hardware names, constants, game concepts, controls, mechanisms, vision, and
  strategy belong in TeamCode.
- Telemetry is current state; logging is event history.
- Robot controls activate only after Teleop Start; config menu works during init.
- Hardware failures isolate their subsystem; stop hooks directly safe actuators.
- Commands claim requirements; deferred commands declare all possible child owners.
- Deploy Sloth accelerates ordinary TeamCode iteration and runs Gradle offline after
  an online full build has primed the computer; Deploy Full removes the overlay and
  remains the authoritative release/competition validation. Both prove
  and, when necessary, repair the configured Robot Controller ADB connection first.
- Owned production code maintains 100% line and branch coverage.

## Style

Prefer concise, readable Kotlin and established local patterns. Keep comparable
members visually consistent; avoid clever one-liners that hide lifecycle or command
ownership. Use typed units for public distance/angle/path-progress APIs. Use
qualified subsystem names for common verbs in coordinating code.

## Completion

Run the exact command in [Verification](rebuild/verification.md). Keep tests with
the behavior they test, organize commits by conceptual dependency, and update the
requirement/architecture/rebuild/traceability documentation when behavior changes.

## Team-facing documentation

Navigation separates Get started, editable Our robot pages, and Platform reference.
Use `.agents/skills/document-robot/SKILL.md` to assist team documentation. The
neutral scaffold remains mecanum. The tank/arcade guide reviews the earlier paired
motor mapping against Pedro 2.1.2, requires robot-centric zero-strafe input, and
records desktop evidence separately from the coach's earlier driving report.
Current upgraded tank hardware, dedicated odometry, and differential autonomous
behavior still require physical validation.

Robot requirements follow the implementation-readiness guide and worked example:
expected behavior, test scenarios, and whole-robot interactions must be explicit.
The documentation skill reports unresolved policy separately from missing hardware
measurements. Teaching examples are not implemented robot features.

## Native hardware adapters

MotorEx, IMUEx, ServoEx, and CRServoEx are aliases to native NextFTC types, with lazy configuration
factories and Kotlin extensions. Subsystem initialization recognizes them directly
as well as Hardware implementations. Device remains the generic SDK-device delegate. Full
upstream APIs are directly visible, without nested motorEx/imuEx forwarding or servo subclasses.
See [Hardware](architecture/hardware.md) for extension imports, constructor
selection, failure labels, and the full-install migration requirement.
