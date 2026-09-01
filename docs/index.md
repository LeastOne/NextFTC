# 3DRD NextFTC

This documentation defines **what** the repository must do, **why** each major
capability exists, and **how** to reconstruct the project from the official FTC
Robot Controller foundation.

The repository has two intentional endpoints:

| Endpoint | Purpose | Stable reference |
|---|---|---|
| Reusable seasonal base | A neutral starting point for a different robot and FTC season | `reusable-season-base` |
| Decode/Osiris implementation | The current robot, hardware, controls, navigation, vision, and autonomous strategy | `main` |

!!! warning "Do not start a new season from the wrong layer"
    The reusable libraries remain useful on `main`, but `main` also contains
    Decode field geometry and Osiris hardware assumptions. Start a new season from
    `reusable-season-base`, then add the new robot's policy in `TeamCode`.

## Choose a path

- **Understand the contract:** begin with [Requirements](requirements/index.md).
- **Learn how the pieces work:** read [Architecture](architecture/overview.md).
- **Recreate the repository:** follow [Rebuild](rebuild/index.md).
- **Start next season:** use [Start a new season](guides/new-season.md).
- **Build the first robot:** follow the end-to-end [first robot](guides/first-robot.md)
  walkthrough and [hardware worksheet](guides/hardware-worksheet.md).
- **Add robot behavior:** use [Add a subsystem](guides/subsystem.md) and
  [Build autonomous behavior](guides/autonomous.md).
- **Find exact values:** use the [Reference](reference/modules-dependencies.md).

## Documentation contract

The Markdown in `docs/` is canonical. The GitHub Pages site is a generated view of
the same files, not a second source of truth. A GitHub wiki is intentionally not
used as the canonical store because wiki content has separate history and review.
If a wiki is enabled, it should contain only a landing page that links here.

Every normative requirement has a stable ID. The
[traceability matrix](requirements/traceability.md) connects requirement groups to
implementation commits, source areas, and tests. Architecture pages explain the
reasoning that does not fit cleanly in a requirement table.

## Baseline and scope

- Upstream: `FIRST-Tech-Challenge/FtcRobotController`
- FTC release: v11.2
- Local foundation: `26cd1fdd`
- Reusable-base tag: `reusable-season-base`
- Android/Kotlin modules: `FtcRobotController`, `3drdNextFTC`,
  `3drdQuanomous`, and `TeamCode`
- Required quality gate: 100% line and branch coverage in the three owned modules

This documentation does not replace FIRST, NextFTC, Pedro Pathing, or Panels
documentation. It records how and why those systems are assembled here.
