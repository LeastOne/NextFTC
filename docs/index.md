# 3DRD NextFTC

This documentation defines **what** the repository must do, **why** each major
capability exists, and **how** to reconstruct the project from the official FTC
Robot Controller foundation.

The repository has two intentional endpoints:

| Endpoint | Purpose | Stable reference |
|---|---|---|
| Reusable seasonal base | A neutral starting point for a different robot and FTC season | Quickstart `bdfc6ad6` |
| Decode/Osiris implementation | The current robot, hardware, controls, navigation, vision, and autonomous strategy | `main` |

!!! warning "Do not start a new season from the wrong layer"
    The reusable libraries remain useful on `main`, but `main` also contains
    Decode field geometry and Osiris hardware assumptions. Start a new season from
    [3DRD NextFTC Quickstart](https://github.com/3DRoboticsDuluth/NextFTC-Quickstart),
    then add the new robot's policy in `TeamCode`.

## Start Here

1. [Start a new season](guides/new-season.md) explains how to create a season
   repository from Quickstart without inheriting Decode or Osiris policy.
2. [Why Kotlin?](guides/why-kotlin.md) explains the language choice, its FTC-specific
   benefits, and the conventions students should follow.
3. [Build the first robot](guides/first-robot.md) takes the neutral scaffold through
   measured Pedro constants, Drive/Nav, a mechanism, deployment, and physical tests.
4. Use the [hardware worksheet](guides/hardware-worksheet.md), [Add a subsystem](guides/subsystem.md),
   and [Build autonomous behavior](guides/autonomous.md) as the robot develops.

## Understand and Reproduce It

- [Architecture](architecture/overview.md) explains how the pieces work.
- [Requirements](requirements/index.md) defines the durable what and why.
- [Rebuild](rebuild/index.md) reproduces both the reusable base and Osiris.
- [Reference](reference/modules-dependencies.md) records exact values and decisions.

## Documentation Contract

The Markdown in `docs/` is canonical. The GitHub Pages site is a generated view of
the same files, not a second source of truth. A GitHub wiki is intentionally not
used as the canonical store because wiki content has separate history and review.
If a wiki is enabled, it should contain only a landing page that links here.

Every normative requirement has a stable ID. The
[traceability matrix](requirements/traceability.md) connects requirement groups to
implementation commits, source areas, and tests. Architecture pages explain the
reasoning that does not fit cleanly in a requirement table.

## Baseline and Scope

- Upstream: `FIRST-Tech-Challenge/FtcRobotController`
- FTC release: v11.2
- Local foundation: `26cd1fdd`
- Reusable Quickstart base: `bdfc6ad6`
- Android/Kotlin modules: `FtcRobotController`, `3drdNextFTC`,
  `3drdQuanomous`, and `TeamCode`
- Required quality gate: 100% line and branch coverage in the three owned modules

This documentation does not replace FIRST, NextFTC, Pedro Pathing, or Panels
documentation. It records how and why those systems are assembled here.
