# Osiris: our Decode robot

This repository contains the Decode-season Osiris implementation on the 3DRD
NextFTC platform. Its hardware, field coordinates, mechanisms, vision, and
autonomous strategy are robot-specific. Start a different robot from
[Quickstart](https://github.com/3DRoboticsDuluth/NextFTC-Quickstart).

## Understand this robot

- [Robot requirements](requirements/decode-osiris.md), with detailed
  [system](requirements/osiris-system.md), [mechanism](requirements/osiris-mechanisms.md),
  [navigation and vision](requirements/osiris-navigation-vision.md), and
  [autonomous](requirements/osiris-autonomous.md) specifications.
- [Hardware and controls](reference/hardware-controls.md).
- [Quanomous architecture](architecture/quanomous.md) and
  [autonomous guide](guides/autonomous.md).
- [Traceability](requirements/traceability.md) connecting requirements to code and tests.
- [Robot reconstruction](rebuild/phase-3.md) and [clean-room guide](rebuild/one-shot.md).

The additional Our robot pages below are inherited editable templates; they do
not replace the existing Osiris specifications. Record physical evidence separately
from desktop test results.

## Get started

These setup guides apply to the neutral Quickstart endpoint. This repository's
main branch already contains Osiris code. The tank recipe is an alternative
specialization, not Osiris's current drivetrain configuration.

1. [Create the season repository](guides/new-season.md) and prove the baseline.
2. [Record the hardware](guides/hardware-worksheet.md): names, ports, directions,
   and safe outputs before writing mechanism code.
3. [Choose the drivetrain and localization](guides/drivetrain.md). Follow the
   mecanum or two-motor tank/arcade route, with drive encoders or dedicated odometry.
4. [Build and test the first robot](guides/first-robot.md).
5. Add a [servo subsystem](guides/subsystem.md) or a
   [motor and sensor subsystem](guides/motor-subsystem.md), one mechanism at a time.
6. [Deploy](guides/deployment.md) and record what actually worked on hardware.

New to Kotlin? Start with [Why Kotlin?](guides/why-kotlin.md).

## Make this your robot's handbook

[Our robot](robot/index.md) is an editable starting place for your team's design.
Record the [requirements](robot/requirements.md), [subsystems and architecture](robot/architecture.md),
and [validation results](robot/validation.md) alongside the implementation.
Unknown facts should stay visibly unknown until the team decides or measures them.

When your team has a robot overview, replace this page with that overview and
keep the Get started links available in navigation. Update the site title in
`mkdocs.yml` and the repository README to identify your team, season, and robot.
The [documentation workflow](guides/document-your-robot.md) includes an agent skill
that helps students develop these pages from design conversations and code.

## Platform reference

The reusable starter still has a complete [platform reference](reference/platform.md):
architecture, stable requirements, reconstruction steps, and dependency details.
Use it when explaining an inherited behavior or rebuilding the platform.
Your robot's requirements belong under Our robot; platform contracts retain their
existing IDs and remain separate.

Markdown in `docs/` is the source of truth. The generated site presents those same
files; document changes belong in code review with their associated implementation.
