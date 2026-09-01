# Osiris system specification

This is the normative reconstruction detail for `REQ-DEC-001`, `REQ-DEC-003`–
`005`, and `REQ-DEC-010`–`014`. Values here describe Osiris and must not be moved
into the reusable phase-2 platform.

## Robot and Pedro constants

The Pedro constants object MUST retain the recognizable upstream builder structure
and construct one mecanum/Pinpoint follower with these values:

| Concern | Required value |
|---|---|
| Robot length | `14.25 in` |
| Robot width | `11.375 in` |
| Drawing radius | `max(length, width) / 2`, therefore `7.125 in` |
| Mass | `12.5628` |
| Forward zero-power acceleration | `-24.591773413810188` |
| Lateral zero-power acceleration | `-76.0984478775747` |
| Translational PIDF | `(0.1, 0.0, 0.0, 0.015)` |
| Heading PIDF | `(0.75, 0.0, 0.0, 0.0075)` |
| Drive filtered PIDF | `(0.02125, 0.0, 0.0000085, 0.51, 0.0085)` |
| Path constraints | `(0.995, 100.0, 0.9, 1.0)` |
| Maximum drive power | `1.0` |
| X velocity | `73.62513937161664` |
| Y velocity | `56.98721866157111` |
| Front-left motor | `driveFrontLeft` |
| Front-right motor | `driveFrontRight` |
| Rear-left motor | `driveBackLeft` |
| Rear-right motor | `driveBackRight` |
| Forward pod Y | `4.7244` |
| Strafe pod X | `1.996` |
| Forward encoder direction | `FORWARD` |
| Strafe encoder direction | `FORWARD` |

`createFollower(hardwareMap)` MUST apply follower constants, path constraints,
mecanum constants, and Pinpoint constants to `FollowerBuilder`, in that order. The
Pinpoint device name currently remains Pedro's default; upgrading Pedro requires
explicitly confirming that default against the Control Hub configuration.

## Shared OpMode composition

All robot OpModes MUST derive from the TeamCode `OpMode`, which configures hardware
telemetry and installs these components in order:

1. `TelemetryComponent`
2. `BindingsComponent`
3. `BulkReadComponent`
4. `PedroComponent(Constants::createFollower)`
5. `PedroDrawingComponent` using the Osiris radius
6. `ConfigComponent` using the live config object
7. every discovered subsystem through `SubsystemComponent.all()`

Teleop MUST be the otherwise-empty `@TeleOp class Teleop : OpMode()`. Auto MUST
schedule the command returned by the Auto subsystem's `execute()` when Start is
pressed. Components and subsystem lists MUST NOT be copied into either concrete
OpMode.

## Game transforms

Alliance and Side MUST provide numeric sign transforms:

| Enum | Sign |
|---|---:|
| `Alliance.UNKNOWN` | `NaN` |
| `Alliance.RED` | `-1.0` |
| `Alliance.BLUE` | `+1.0` |
| `Side.UNKNOWN` | `NaN` |
| `Side.NORTH` | `+1.0` |
| `Side.SOUTH` | `-1.0` |

Invoking either enum with a number multiplies the number by its sign. Code needing
the selected enum uses `Config.alliance` or `Config.side`; code needing a mirrored
number invokes that enum. Unknown deliberately contaminates accidental coordinate
math with `NaN` rather than silently selecting a field side.

## Driver configuration

The persistent config object MUST declare settings in this order:

| Setting | Default and metadata |
|---|---|
| Alliance | `UNKNOWN`, init-only |
| Side | `UNKNOWN`, init-only |
| Quanomous | `null`, init-only, options supplied by reusable Quanomous storage |
| Delay | `0.0`, increment `0.5`, range `0.0..30.0`, format `%.1fs`, init-only |
| Responsiveness | `1.0`, increment `0.05`, range `0.0..1.0`, format `%.2f`, live |
| Robot Centric | `true`, live |
| Park Gate | `false`, init-only |
| Goal Distance Offset South | `0.0`, increment `6.0`, format `%.1f in`, live |
| Goal Distance Offset North | `0.0`, increment `6.0`, format `%.1f in`, live |
| Goal Angle Offset South | `0.0`, increment `1.0`, format `%.1f deg`, live |
| Goal Angle Offset North | `0.0`, increment `1.0`, format `%.1f deg`, live |
| Level | `INFO`, live |
| Filter | empty string, transient and not a Driver Station setting |

During Auto initialization, Alliance and Side MUST reset to `UNKNOWN` and
Quanomous MUST reset to `null`. While Auto is initializing, a warning above the
configuration section MUST list every missing selection. Starting Auto MUST fail
with the same actionable list until all three are selected.

Changing Alliance or Side MUST call the reusable `resetStartingPose(Nav.start)`
operation, which updates both Pedro's starting reference and current pose. Auto
Start MUST NOT reset it again: movement tracked after field selection is valid.

## Driver-controlled motion

### Tunables

| Value | Default |
|---|---:|
| Forward PID | `(0.035, 0.005, 0.005)` |
| Strafe PID | `(0.025, 0.005, 0.005)` |
| Heading PID | `(0.35, 0.0015, 0.05)` radians |
| Forward/strafe/turn static compensation | `0.05` each |
| Heading `kS`, `kV`, `kA` | `0.0`, `0.0`, `0.0` |
| Still acceleration threshold | `2.0` |
| Intake/low/medium/high/auto power | `0.5`, `0.5`, `0.75`, `1.0`, `1.0` |
| Too-far threshold | `3 tiles` |
| Goal-lock maximum turn | `0.4` |

### Inputs and modes

The long-lived default driver command MUST evaluate live suppliers on every update:

- forward = negative gamepad-1 left-stick Y;
- strafe = negative gamepad-1 left-stick X;
- turn = negative gamepad-1 right-stick X;
- robot-centric = configured robot-centric and chase is not locked;
- field-centric heading offset = zero in robot-centric/chase/unknown-alliance mode,
  otherwise `alliance(-90°)`.

The command MUST require Drive. It is the default only in Teleop. Initialization
captures the three gamepad ranges, chooses auto/high scalar by OpMode type, clears
both locks, and resets the still timer. D-pad down, either side, and up select low,
medium, and high while Back is not held; Back+Start toggles centric mode.

### Assist behavior

Goal lock may become true only after Start, in field-centric mode, with known
Alliance and Side. Chase lock is independently settable and restarts Pedro Teleop
drive when enabled.

With no chase target, forward/strafe pass through unchanged. With a target, each
axis passes through inside one inch; otherwise Next Control calculates from the
remaining position and follower velocity, then subtracts
`sign(remaining) * axisKS`.

Turn passes through unless goal lock is active or chase lock has an element. Chase
turn also passes through inside two degrees. Otherwise it uses the heading
controller, clamps correction to `±0.4`, and adds heading feedforward based on
lateral velocity and acceleration.

`isStill()` compares follower acceleration magnitude with `2.0` and resets its
timer whenever moving. The timed form becomes true only after continuous stillness.
`isAtElement()` is true when no element exists or distance to the artifact target
is below the configured element radius. `isTooFar()` applies only during Teleop.

### Diagnostics and stop

Drive MUST show power at INFO; X, Y, and heading at DEBUG with fixed precision; and
busy, still, goal lock, and chase lock at VERBOSE. Lifecycle stop MUST immediately
break Pedro following. The command-level `stop` does the same through a delegated
instant command.

## Physical acceptance

Automated tests MUST cover constants, game signs, config reset/readiness, input
mapping, both centric modes, command requirements, corrections, status helpers,
telemetry, follower operations, and stop. Robot acceptance additionally requires:

- forward/strafe/turn agree with stick intent;
- field-centric orientation is correct for red and blue Driver Station viewpoints;
- Pinpoint pose and Panels drawing agree through translation and rotation;
- repeated init/stop cycles do not duplicate bindings; and
- Auto never accepts manual drive controls.
