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
| Flywheel | `flywheelLeft` | `MotorEx`, reversed, `RUN_WITHOUT_ENCODER`, float at zero |
| Flywheel | `flywheelRight` | `MotorEx`, forward, `RUN_WITHOUT_ENCODER`, float at zero |
| Kickstand | `kickstandLeft` | `ServoEx`, logical range `0.25..0.95` |
| Kickstand | `kickstandRight` | `ServoEx`, logical range `0.30..1.00`, reversed |
| Lights | `prism` | unchanged vendor `GoBildaPrismDriver` |

A missing device MUST disable only its owning subsystem through the reusable
hardware/subsystem lifecycle. Vendor Prism Java sources remain in their supplied
language and recognizable structure.

Servo declarations use the inherited fluent range bridge, for example
`ServoEx("deflector").scaleRange(MIN, MAX).reversed()`. Deflector, Gate, bumper,
kickstand, and turret ranges retain the values above and the native ServoEx type.
Range arguments are captured at declaration and applied lazily on first acquisition;
these are not live range bindings. Kickstand's right servo keeps its explicit SDK
`direction = REVERSE` block before the fluent range call, preserving assignment
semantics instead of changing to a direction toggle. No initial position writes
are added by this refactor. See [fluent range configuration](../architecture/hardware.md#fluent-servo-range-configuration).

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
| NextControl velocity PID | `(1.0, 0.0, 0.0)` |
| Motor feedforward `kV`, `kA`, `kS` | `1.0`, `0.0`, `0.0` |
| Maximum reference acceleration | `1.6` normalized velocity units/second |
| Maximum reference jerk | `16.0` normalized velocity units/second squared |
| Current threshold | `5.0` A per motor |
| Power-cap reduction/recovery rate | `2.0` / `0.5` power units/second |
| Axial feedforward `kS`, `kV`, `kA` | `0.0`, `0.0008`, `0.0` |
| Forward/reverse/hold/stop | `0.4`, `-0.5`, `-0.2`, `0.0` |
| Ready tolerance | `0.05` normalized speed, above and below target |
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

This branch explores NextFTC software control rather than reproducing the deployed
Hub PIDF loop. Every periodic cycle recalculates and bounds the normalized target
to `-1..1`. Each motor has its own NextControl `ControlSystem` using `velPid` and
`basicFF`, with encoder feedback from `MotorEx.velocityPercentage`. Output is
bounded power through the native NextFTC motor API. `RUN_WITHOUT_ENCODER` disables
the Hub velocity controller; NextFTC selects it when writing power, and encoder
readings remain available. Fluent `floatMode()` declarations retain lazy hardware
initialization. The left motor uses NextFTC `reversed()` rather than SDK direction:
native power and velocity have the intended logical sign, while raw `.motor`
readings and the existing SDK-based motor telemetry retain the physical sign.
The mutable PID
and feedforward parameter objects are shared, but controller history is independent.
The example gains demonstrate the architecture; they are not calibrated robot gains.
The measured `KineticState` contains velocity only; measured acceleration is zero.
Each controller has its own jerk-limited `SCurve` velocity interpolator, which
supplies reference velocity and acceleration. Motor `kA` can use that reference;
the example leaves `kA` and `kD` zero, without claiming derivative feedback from
measured acceleration.
The axial compensation above adjusts the requested speed; motor feedforward
converts that requested speed into power.

Both profiles receive the final bounded target. At `PROFILE.maxAcceleration = 1.6` and
`PROFILE.maxJerk = 16.0`, reference speed rises from zero to `0.4` in 350 ms. Profiles limit jerk and acceleration,
deceleration, and reversal; the independent feedback loops can still produce
different powers. Each requested power passes through its own `CurrentLimiter`
using a fresh SDK current reading in amperes. Above `CURRENT.maxCurrent`, the cap falls
at `CURRENT.reductionRate`; at or below the threshold it recovers at `CURRENT.recoveryRate`.
The cap remains `0..1` and clips either sign of power. These example settings
illustrate the experiment, not calibrated motor ratings. This sampled response
does not guarantee a current ceiling or prevent brief spikes; see
[control utilities](../architecture/control.md) for the full limiter contract.

`PROFILE` is a shared `SCurveParameters` object and `CURRENT` is a shared
`CurrentLimitParameters` object, declared beside `PID` and `FF`. Panels edits their
mutable nested properties directly. Each motor's profile/limiter reads that shared
configuration while retaining independent motion state and power cap, without
tuning lambdas, clock arguments, or per-cycle property copying.
Changes take effect without reinitialization or resetting profile/limiter state.
Initialization resets the existing controllers and limiters. The profiles use
`SCurve`; the simpler library alternatives remain `VelocityRamp` and `VelocitySmoother`.
Retargeting preserves reference velocity and acceleration. A sudden nearby/reversed
goal can require temporary overshoot while acceleration is removed. A lowered live
acceleration limit is approached at bounded jerk rather than erasing acceleration.
Edits to the shared PID/FF coefficient objects remain live. Timing is internal to
the library. Integration tests replace and restore private clocks only in test
code; Flywheel has no public clock seam or timing declarations.

A zero target resets both profiles/controllers and current limiters and writes
zero power, allowing coasting without ramping down or actively braking.
Ready requires a positive target and both measured normalized speeds within the
absolute tolerance of the final target, not the intermediate ramp reference;
overspeed also fails readiness. `untilReady()` retains its one-second maximum.
Lifecycle stop clears targeting, selects stop, resets profiles and limiters,
and directly writes zero to both motors without waiting for another periodic call.
Restarting after a stop begins the profile from rest, even after a long idle period.

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
