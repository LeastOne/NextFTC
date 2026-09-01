# Project context

This page is the concise handoff for mentors, contributors, and coding agents. The
linked pages are canonical when more detail is needed.

## Purpose

The repository begins with FTC Robot Controller v11.2, inherits the reusable 3DRD
Kotlin/NextFTC platform and neutral TeamCode scaffold from Quickstart `bdfc6ad6`,
then adds the Decode/Osiris robot. Current `main` is not a neutral season template.

## Read First

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
