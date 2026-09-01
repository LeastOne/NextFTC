# Phase 3 — Decode and Osiris

Phase 3 turns the neutral launch point into the current robot. Each conceptual
commit includes its tests, and no phase-3 behavior is required by another season.

For clean implementation, use the exact normative specifications alongside this
dependency order: [system](../requirements/osiris-system.md),
[mechanisms](../requirements/osiris-mechanisms.md),
[navigation and vision](../requirements/osiris-navigation-vision.md), and
[autonomous](../requirements/osiris-autonomous.md). This page explains when to add
each concern; those pages define the values and observable behavior.

## 1. Navigation foundation — `6b7e0567`

Replace template Pedro constants with Osiris hardware names, dimensions, mass,
Pinpoint offsets, constraints, and tuned controller values. Add Decode Alliance and
Side transforms. Specialize Config, Drive, Nav, and Vision. Update the OpMode only
for robot-specific integration such as its constants/radius.

Key behavior to reproduce:

- map left-stick Y/X and right-stick X to forward/strafe/turn with verified signs;
- allow live robot-/field-centric mode and speed scalar;
- use Next Control for goal/chase corrections;
- keep embedded motor velocity control available for later flywheel use;
- define field poses and paths with tiles, angles, alignment, alliance, and side;
- reset both starting and current Pedro pose when alliance/side changes;
- operate Limelight pipelines and convert detections to field poses;
- require explicit Alliance, Side, and Quanomous selection before Auto start.

Checkpoint: constants, game, OpMode, config, drive, nav, and vision tests pass; then
validate stick directions, field-centric driving, localization, and Panels drawing
on Osiris.

## 2. Deflector and Gate — `49f3fa67`

Add the two servo subsystems with configurable ranges/positions, delegated commands,
operator chord bindings, periodic hardware update, and one test file per subsystem.

Checkpoint: test on hardware that configured direction/range produce the expected
physical positions.

## 3. Artifact handling and launching — `0dbce6ae`

Add Intake, Conveyor, and Flywheel:

- Intake coordinates roller, bumpers, laser debounce, artifact count, and waits.
- Conveyor supports transport plus distance-based launch velocity.
- Flywheel uses hub velocity PIDF plus distance/robot-motion target calculation and
  readiness wait.
- Motor-owning subsystems write safe power directly from `stop()`.

Checkpoint: dedicated tests pass, then validate direction, sensors, velocities,
artifact counts, and shutdown on Osiris.

## 4. Kickstand and lights — `b4f08db0`

Add paired Kickstand servos and controls. Import the GoBilda Prism Java driver and
supporting vendor types without Kotlin conversion. Add Lights state precedence for
intake-full, match-time warnings, and alliance.

Checkpoint: verify paired servo geometry, both gamepad bindings, LED count/animation,
and precedence on hardware.

## 5. Decode autonomous strategy — `297645cd`

Add the command-focused Auto subsystem and the TeamCode Quanomous compiler subsystem.
Compile data steps to deferred/composed commands for intake, deposit, gate, chase,
drive, and parking. Auto OpMode schedules `execute()` at Start. Enforce delay,
29.5-second timeout, remaining-time wrappers, and explicit stop-all.

Checkpoint: all Auto/Quanomous tests pass, then dry-run every stored program with
the robot lifted or mechanisms made safe before field testing.

## 6. Project context and documentation — `a43b2915` onward

Record repository policy and architecture context. The current `docs/` system then
expands that context into requirements, rationale, reconstruction, guides, and
reference, and publishes the same Markdown through GitHub Pages.

## Exact implementation replay

```powershell
git cherry-pick 6b7e0567 49f3fa67 0dbce6ae b4f08db0 297645cd a43b2915
```

Then apply the later documentation commits shown on `main`.

## Phase gate

Run the complete verification command and documentation build. Automated tests do
not replace physical validation. Record at least:

- forward, strafe, and turn direction;
- robot- and field-centric behavior for both alliances;
- Pinpoint pose and Panels drawing agreement;
- every servo/motor direction and safe stop;
- laser, Limelight, and Prism connection/behavior;
- required Auto selection warning and refusal;
- each intended Quanomous strategy at reduced risk before full-speed field runs.
