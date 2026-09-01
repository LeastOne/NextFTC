# Osiris mechanism specification

This page provides the exact reconstruction contract for `REQ-DEC-020`–`026`.
Uppercase mutable values are Panels tuning surfaces. They are defaults, not Kotlin
constants, and MUST remain adjustable unless the design deliberately changes.

## Hardware map

| Owner | Device name | Required type/configuration |
|---|---|---|
| Deflector | `deflector` | `ServoEx`, logical range `0.0..0.8`, reversed |
| Gate | `gate` | `ServoEx`, logical range `0.0..0.5`, reversed |
| Intake | `intake` | `MotorEx`, reversed, `RUN_USING_ENCODER`, float at zero |
| Intake | `laser2` | digital channel configured input |
| Intake | `bumperLeft` | `ServoEx`, logical range `0.5..1.0` |
| Intake | `bumperRight` | `ServoEx`, logical range `0.0..0.5` |
| Conveyor | `conveyor` | `MotorEx`, `RUN_USING_ENCODER`, float at zero |
| Flywheel | `flywheelLeft` | `MotorEx`, reversed, `RUN_USING_ENCODER`, float at zero |
| Flywheel | `flywheelRight` | `MotorEx`, forward, `RUN_USING_ENCODER`, float at zero |
| Kickstand | `kickstandLeft` | `ServoEx`, logical range `0.25..0.95` |
| Kickstand | `kickstandRight` | `ServoEx`, logical range `0.30..1.00`, reversed |
| Lights | `prism` | unchanged vendor `GoBildaPrismDriver` |

A missing device MUST disable only its owning subsystem through the reusable
hardware/subsystem lifecycle. Vendor Prism Java sources remain in their supplied
language and recognizable structure.

## Deflector

| Tuning value | Default |
|---|---:|
| `MIN`, `MAX` | `0.0`, `0.8` |
| `POS` | `0.5` |
| `INC` | `0.02` |

`up` adds `INC`; `down` subtracts it. Movement clamps logical `POS` to `0.0..1.0`.
During Teleop, hold gamepad-2 Y without Start and press D-pad up/down. Every
periodic cycle writes `POS` through the servo `update` operation. The current
position remains until moved; initialization MUST NOT invent an extra servo write.

## Gate

| Tuning value | Default |
|---|---:|
| `MIN`, `MAX` | `0.0`, `0.5` |
| `CLOSE`, `HOLD`, `OPEN` | `0.0`, `0.5`, `1.0` |
| `POS` | `CLOSE` |

Delegated `open`, `hold`, and `close` commands select the corresponding live value.
During Teleop, hold gamepad-2 Back without Start and use D-pad up/down to open/close.
Periodic writes `POS`. The delegated command names and execution logging MUST be
`Gate.open`, `Gate.hold`, and `Gate.close` without handwritten duplicate log calls.

## Intake

### Values and commands

| Value | Default |
|---|---:|
| Stop/hold/forward/reverse velocity percentage | `0.0`, `0.50`, `1.0`, `-0.25` |
| Current `VEL` | stop |
| Maximum artifacts | `3` |
| Laser debounce threshold | `0.125 s` |
| Left bumper current/min/max | `0.0`, `0.5`, `1.0` |
| Right bumper current/min/max | `1.0`, `0.0`, `0.5` |

`forward`, `reverse`, `hold`, and `stop` select velocity. `reset` sets artifact
count to zero and `full` false. Initialization selects stop, clears count/full, and
resets debounce. Hold gamepad-2 A without Start and use D-pad up/down/side for
forward/reverse/stop.

### Bumper geometry

Each periodic cycle computes one logical bumper position:

1. If Euclidean distance from follower pose to `Nav.gateIntake` is less than one
   tile, use `1.0` on red and `0.0` otherwise.
2. Otherwise use
   `1 - max(0, sign(y) * sign(abs(y) - abs(x)))` from the follower pose.
3. Assign that result to both bumper tuning positions.
4. While started and `VEL > HOLD`, write the computed position to each bumper.
   Otherwise retract left to `1.0` and right to `0.0`.

The roller receives `VEL` as velocity percentage. The current digital `laser.state`
is treated as blocked. On a debounced blocked transition, increment artifacts only
while below the maximum and set `full` when the increment reaches the maximum.
INFO telemetry reports artifact count; DEBUG reports laser state.

`untilElement()` snapshots `artifacts + 1` when scheduled and waits for that count
or full. `untilFull()` waits for full; `untilArtifacts(n)` waits for count at least
`n`. Lifecycle stop selects stop and directly writes zero motor power.

## Conveyor

| Value | Default |
|---|---:|
| Stop/forward/reverse | `0.0`, `0.5`, `-0.2` |
| Current `VEL` | stop |

The corresponding delegated commands select those values. `launch` evaluates the
following percentage from current follower-to-goal distance `d` when the command
runs:

```text
1.158969 + (0.7886366 - 1.158969) / (1 + (d / 116.6622)^2.41902)
```

Periodic applies velocity percentage. Hold gamepad-2 X without Start and use D-pad
up/down/side for forward/reverse/stop. `untilStopped()` waits for `VEL == STOP`.
Lifecycle stop selects stop and writes zero power immediately.

## Flywheel

### Values and hardware

| Value | Default |
|---|---:|
| Embedded velocity PIDF | `(64.0, 0.0, 0.0, 8.0)` |
| Axial feedforward `kS`, `kV`, `kA` | `0.0`, `0.0008`, `0.0` |
| Forward/reverse/hold/stop | `0.4`, `-0.5`, `-0.2`, `0.0` |
| Ready threshold | `0.85` of requested speed |
| Initial target/current mode | stop, not targeting |

`launch` enables distance targeting and seeds forward velocity. The other four
commands disable targeting and select their fixed value. Either gamepad-2 trigger
above `0.5` runs forward while held and stops on release. Alternatively, hold B
without Start and use D-pad up/down/side for forward/reverse/stop.

When targeting, velocity percentage is:

```text
1.064487 + (0.2797805 - 1.064487) / (1 + (distance / 177.5942)^3.400669)
+ kS * sign(axialVelocity) + kV * axialVelocity + kA * axialAcceleration
```

Every periodic cycle recalculates the value, applies the PIDF to each SDK motor,
sets ticks/second to `achievableMaxTicksPerSecond * VEL`, and reports motor
telemetry. Embedded Hub velocity control is intentional; Next Control is used for
lower-rate robot-axis assists, not to replace this motor loop.

Ready means both measured motor velocities are at least
`achievableMaxTicksPerSecond * VEL * 0.85`. `untilReady()` has a one-second maximum.
Lifecycle stop clears targeting, selects stop, and directly writes zero to both.

## Kickstand

The left/right servo ranges are `0.25..0.95` and `0.30..1.00`; right is reversed.
Logical disengage is `0.0`, engage is `1.0`, and initialization disengages.
Delegated commands engage, disengage, or toggle exact equality with the engage
value. Simultaneously pressing both bumpers on either gamepad, without that
gamepad's Start button, toggles. Periodic writes the same logical position to both.

## Lights

The Prism strip length is `48`. Configuration MUST:

1. set that strip length;
2. install solid red on layer 0;
3. save current animations to artboard 0;
4. select artboard 0 as boot default; and
5. enable the default boot artboard.

`show(color)` suppresses duplicate updates. Each periodic cycle applies the first
matching rule:

| Priority | Condition | Color |
|---:|---|---|
| 1 | Intake full | green |
| 2 | started Teleop and runtime > 110 s | red |
| 3 | started Teleop and runtime > 100 s | orange |
| 4 | started Teleop and runtime > 80 s | white |
| 5 | red alliance | red |
| 6 | blue alliance | blue |

LED count and FPS are DEBUG telemetry. Match warnings MUST NOT run during Auto or
before Start. `set(color)` remains an explicit named command for composition.

## Timing

Endgame begins at `75.0 s`. Initialize resets play/periodic timers and the one-shot
rumble flag; Start resets only play time. At or after endgame, periodic rumbles both
gamepads at full left/right intensity for 1000 ms exactly once.

DEBUG telemetry reports runtime seconds with one decimal, loop milliseconds as a
whole number, and rate Hz with one decimal. Rate is `1000 / loopMilliseconds`; the
periodic timer resets after reporting.

## Mechanism acceptance

Each subsystem MUST have one corresponding test class in the subsystem package.
Tests MUST assert configured hardware, every command, controls, initialization,
periodic output/calculation branches, waits, telemetry, and direct stop behavior.
Physical validation MUST record servo endpoints, motor directions, sensor polarity,
launch readiness, light precedence, and stop behavior with the robot made safe.
