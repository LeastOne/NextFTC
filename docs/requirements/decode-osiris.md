# Decode and Osiris requirements

These requirements intentionally apply **after** the reusable seasonal base. They
describe the current robot and game, not generic 3DRD platform behavior.

This page is the concise requirement index. The normative values and algorithms
needed for an independent reconstruction are in:

- [Osiris system specification](osiris-system.md)
- [Osiris mechanism specification](osiris-mechanisms.md)
- [Osiris navigation and vision](osiris-navigation-vision.md)
- [Osiris autonomous specification](osiris-autonomous.md)

Those pages refine, but do not replace, the stable requirement IDs below.

## Field, robot, and setup

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-DEC-001` | Pedro constants MUST describe Osiris dimensions, mass, motors, Pinpoint localizer, offsets, and tuned constraints/controllers. | Accurate path following and field drawing require the physical robot model. | Constants tests verify the configured values and follower construction. |
| `REQ-DEC-002` | Decode navigation MUST express field geometry in tiles, typed distances, angles, alliance mirroring, and side mirroring. | A single readable coordinate model avoids four duplicated alliance/side maps. | Alliance, Side, Nav, and pose tests cover signs and locations. |
| `REQ-DEC-003` | Selecting alliance or side during init MUST reset Pedro's starting and current pose together. | Moving between known starts must not preserve an unwanted localization offset. | Config/Nav tests verify `resetStartingPose(Nav.start)`. |
| `REQ-DEC-004` | Auto init MUST reset Alliance, Side, and Quanomous selection and MUST refuse start until all three are selected. | Autonomous must not silently run a persisted strategy for the wrong field setup. | Config tests cover reset, warning, and start failure. |
| `REQ-DEC-005` | The Driver Station MUST warn above configuration while required Auto selections are missing. | Drivers need an actionable setup indication before pressing Start. | Telemetry/config tests verify warning content and placement. |

## Driving and sensing

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-DEC-010` | Teleop drive MUST use left-stick forward/strafe and right-stick turn with Osiris-correct signs. | Driver intent must map directly to mecanum motion. | Drive tests verify input mapping and sign. |
| `REQ-DEC-011` | Driver control MUST support runtime speed levels and robot-/field-centric selection. | Drivers need precision near game elements and speed while traversing the field. | Drive control tests cover scalar and centric behavior. |
| `REQ-DEC-012` | Goal and artifact assists MUST use Next Control feedback while preserving manual control when a target is unavailable or within tolerance. | Assisted alignment should help rather than unexpectedly seize the robot. | Drive tests cover goal/chase states, corrections, and fallback. |
| `REQ-DEC-013` | Vision MUST operate the Limelight pipelines and turret, translate detections into Pedro field poses, and stop the camera with the OpMode. | Navigation and launching depend on consistent field-relative targets and safe resource cleanup. | Vision tests cover pipeline switching, conversions, locks, and stop. |
| `REQ-DEC-014` | Panels MUST draw the robot using Pedro's field coordinate convention and Osiris radius. | Visualization is useful only when it agrees with the localizer and physical footprint. | OpMode, Constants, and drawing tests pass; robot testing confirms orientation. |

## Mechanisms

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-DEC-020` | Deflector and Gate MUST expose delegated commands, configure servo ranges/directions, and apply their current position in `periodic()`. | Teleop and Auto share concise behavior while Panels can tune positions. | Dedicated tests cover commands, controls, initialization, and periodic output. |
| `REQ-DEC-021` | Intake MUST coordinate the roller, two bumpers, and laser-debounced artifact count, and stop its motor directly on shutdown. | Collection requires geometry-aware bumpers and reliable edge detection; stop cannot depend on another loop. | Intake tests cover hardware, count, commands, waits, telemetry, and stop. |
| `REQ-DEC-022` | Conveyor MUST support forward/reverse/stop and distance-based launch velocity, and stop directly on shutdown. | Transport and launch speed have different needs while sharing one motor. | Conveyor tests cover calculation, controls, periodic output, waits, and stop. |
| `REQ-DEC-023` | Flywheel MUST support manual and target-based velocity, apply embedded motor velocity PIDF, report readiness, and stop both motors directly. | Hub-level velocity control is appropriate for the high-rate flywheel loop. | Flywheel tests cover targeting, feedforward, readiness, telemetry, and stop. |
| `REQ-DEC-024` | Kickstand MUST drive mirrored servos through engage/disengage/toggle commands from either operator. | A paired mechanism should present one conceptual state. | Kickstand tests cover configuration, bindings, and output. |
| `REQ-DEC-025` | Lights MUST use the vendor GoBilda Prism Java driver unchanged and show alliance, match-time, and intake-full status. | Vendor code should remain comparable to its source while LEDs communicate robot state. | Lights tests cover precedence and device configuration. |
| `REQ-DEC-026` | Timing MUST report runtime, loop time, and rate, and rumble both gamepads once at endgame. | Drivers and developers need match timing plus loop-health visibility. | Timing tests cover formatting, reset, and one-shot rumble. |

## Autonomous

| ID | Requirement | Why | Acceptance |
|---|---|---|---|
| `REQ-DEC-030` | Decode Auto MUST load the selected Quanomous program after the configured delay, enforce a match timeout, and stop mechanisms afterward. | Strategy is data-driven but must still end safely within the FTC period. | Auto and Quanomous tests cover sequence and timeout. |
| `REQ-DEC-031` | Quanomous step names MUST compile to live NextFTC command compositions for intake, deposit, release, chase, park, and direct drive. | Strategy files should describe intentions rather than Kotlin implementation detail. | Quanomous subsystem tests cover every handler and invalid input. |
| `REQ-DEC-032` | Autonomous drive paths MUST be deferred so pose, alliance, side, target, and configuration are evaluated when the command begins. | The robot may move during init and live vision/config data can change. | Drive/Auto tests verify late evaluation. |
| `REQ-DEC-033` | Stop-all behavior MUST explicitly release assists and directly stop drive and mechanisms. | Interruption or timeout must leave actuators in a known safe state. | Auto and subsystem stop tests verify the shutdown chain. |
