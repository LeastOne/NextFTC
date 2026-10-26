# Public API contract

The requirements describe behavior, while this page fixes the source-level
contract that other modules, tests, and future season repositories may compile
against. A clean-room implementation may organize private code differently, but
it MUST preserve these packages, public entry points, ownership rules, and test
seams unless a deliberate breaking change updates this page and its tests.

## Reusable module packages

| Concern | Canonical package |
|---|---|
| FTC file storage and typed persistence | `org.firstinspires.ftc.threedrd.ftc` |
| NextFTC commands | `org.firstinspires.ftc.threedrd.nextftc.commands` |
| configuration metadata and components | `org.firstinspires.ftc.threedrd.nextftc.config` |
| hardware wrappers and update helpers | `org.firstinspires.ftc.threedrd.nextftc.hardware` |
| velocity interpolators | `org.firstinspires.ftc.threedrd.nextftc.control.interpolators` |
| software current limiter | `org.firstinspires.ftc.threedrd.nextftc.control` |
| logging | `org.firstinspires.ftc.threedrd.nextftc.logging` |
| active-OpMode helpers | `org.firstinspires.ftc.threedrd.nextftc.opmodes` |
| subsystem lifecycle and reusable Drive/Nav bases | `org.firstinspires.ftc.threedrd.nextftc.subsystems` |
| telemetry | `org.firstinspires.ftc.threedrd.nextftc.telemetry` |
| Pedro units, poses, follower, drawing, and driver control | `org.firstinspires.ftc.threedrd.pedropathing` |
| generic debounce utility | `org.firstinspires.ftc.threedrd.util` |
| Quanomous parsing, storage, and compilation | `org.firstinspires.ftc.threedrd.quanomous` |

## Fluent servo configuration

The project hardware package exposes `ServoEx.scaleRange(min, max): ServoEx` for
instances created by its ServoEx factories. It returns the same native object,
queues SDK configuration until first acquisition, and applies later calls immediately.
Its private weak-keyed `configurations` bridge uses no reflection or subclass.
Unregistered upstream-created instances fail without eager lookup. Numeric range
arguments are captured at declaration; no live range binding is implied. Import
the extension explicitly or through the package wildcard. Existing constructors
and SDK configuration blocks remain available; see the hardware architecture.

## Control utilities

`VelocitySmoother`, `VelocityRamp`, and `SCurve` implement NextControl's `goal`,
`currentReference`, and `reset()`. Their public constructors accept the corresponding
`VelocitySmootherParameters`, `VelocityRampParameters`, or `SCurveParameters` object,
or fixed numbers. `CurrentLimiter` accepts `CurrentLimitParameters` or fixed
`maxCurrent`, `reductionRate = 2.0`, and `recoveryRate = 0.5` values; it exposes
`limit(power, current)`, read-only `powerLimit`, and `reset()`.

All four expose their shared tuning object as `parameters`. Parameter fields are
mutable and discoverable through an owning Panels `@Configurable` object. Objects
validate initial settings; utilities read and validate them on update. Sharing
parameters must not share motion/limiter state. This replaces tuning suppliers and
public clock arguments: pass `SCurve(PROFILE)` and edit `PROFILE.maxJerk` directly.

Production timing is internal. Library tests use internal clock-injecting
constructors; robot integration tests can replace private clocks in test code
without adding timing machinery to the subsystem. Reset clears history without
reading settings. See [Control](../architecture/control.md) for normative units,
retargeting, live-limit behavior, and experimental current-limiting limitations.

## Quanomous

`Quanomous` is a Kotlin singleton. It publicly provides `decode`, `parse`,
`filename`, `process`, `options`, `load`, and an eight-character `hash` operation.
Its replaceable `storage`, `now`, and `decoder` dependencies and its `lastHash` and
`lastName` state are intentional test and integration seams. Processing a valid
gzip/Base64 QR payload stores timestamped JSON and suppresses the immediately
repeated hash.

`QuanomousStorage.directory` is readable. `save` and `load` exchange Gson
`JsonArray` values; option discovery ignores non-JSON files and sorts JSON names
case-insensitively. `QuanomousCompiler.load(name)` compiles the named stored
program, and an unknown command MUST raise `IllegalArgumentException` containing
that command name.

## Commands and subsystems

- Delegated instant and deferred commands infer the qualified name
  `Subsystem.property`. Deferred factories support the zero-, one-, and
  three-argument forms used by the robot.
- Commands retain their owner, requirements, and factory/action seams. A command
  owned by a disabled subsystem MUST NOT execute its action.
- Composition and repetition preserve child requirements and lifecycle. Repeated
  children restart only after normal completion and are stopped when interrupted.
- `Subsystem` exposes lifecycle state, `enabled`/`disabled`, errors, telemetry and
  logging facades, command factories, and reflective hardware initialization.
  Discovery includes Kotlin singleton static fields as well as instance fields.
- `SubsystemComponent` exposes discovery/cache seams, discovers lazily during
  `preInit`, applies explicit ordering, enables robot controls only after Teleop
  starts, schedules defaults only during active updates, and cleans up in reverse
  order while isolating failures.
- `ConfigSubsystem` exposes menu state and commands. `ConfigComponent<T>` exposes
  its config and persistence dependencies plus the global change hook. Setting
  reflection preserves primary-constructor order, annotations, formats, options,
  live restrictions, and diagnostics fields.

## Hardware and diagnostics

Hardware wrappers expose the underlying configured FTC device and the fluent
operations required by TeamCode: direction, zero behavior, scaled range,
run-to-position, brake/float, power, velocity percentage, update, and telemetry.
`ServoEx` and generic `Device` resolve lazily if their public device accessors are
used before lifecycle initialization. One failed device disables only its owning
subsystem.

`HardwareTelemetry` provides replaceable receiver-lambda hooks for servo,
continuous servo, motor, IMU, and generic devices. The reusable library owns the
hook; TeamCode owns captions, levels, precision, and displayed fields.

Telemetry and logging expose mutable level, filter, and display seams. Entries are
pipe-delimited and support section titles and frame lifecycle. Telemetry represents
the current frame; logging retains bounded history and sends every non-`OFF` event
to RobotLog regardless of Driver Station filtering. Command snapshots omit null
commands.

## Pedro and reusable drive/navigation

`PedroDriverControlled` appends optional `slew: DriverSlewParameters? = null` after
the heading-offset supplier. Existing callers retain the legacy transfer behavior.
`DriverSlewParameters` exposes shared `translation` and `turn` SCurveParameters,
with mutable normalized-command limits. Each opted-in command owns independent
state and internal timing. Input shaping follows NextFTC scaling and precedes
the existing `drive`, `strafe`, and `turn` callbacks. See the
[driver-shaping contract](../architecture/drive-navigation.md#optional-driver-command-shaping)
for frame conversion, lifecycle, and live-edit behavior. No current controller or
electrical ceiling is part of this API.

The Pedro package exposes `TILE_WIDTH` as 23.5 inches, `tile`/`tiles` unit helpers,
pose-relative transformations, normalized headings, typed distance and path
progress, follower pose reset, drawing, and driver-control commands.

`DriveSubsystem` exposes named follow, path, line, curve, multi-curve, relative
move, and turn commands; hold/stop; maximum-power and hold-end overloads; and wait
conditions for distance, completion, path `t`, and follower busy state. A
single-target curve inserts a midpoint; explicitly built Pedro paths use the
library's hold-end overload.

## TeamCode packages

| Concern | Canonical package |
|---|---|
| game enums | `org.firstinspires.ftc.teamcode.game` |
| Auto and Teleop OpModes | `org.firstinspires.ftc.teamcode.opmodes` |
| robot subsystems, including `Config` | `org.firstinspires.ftc.teamcode.subsystems` |
| Osiris Pedro constants | `org.firstinspires.ftc.teamcode.adaptations.pedropathing` |
| robot hardware telemetry policy | `org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware` |
| unmodified goBILDA Prism Java source | `org.firstinspires.ftc.teamcode.adaptations.gobilda.prism` |

The shared OpMode composes telemetry, exactly one bindings component, bulk reads,
the Pedro follower and drawing component, typed configuration, and lazy subsystem
discovery. Auto schedules its selected routine at Start. Teleop remains
constructible with the canonical FTC annotation and class name.

## Osiris configuration and subsystem surface

`subsystems.Config` is the configurable `ConfigSubsystem` singleton. Its nested
persistent `Config` primary constructor defines, in order: alliance, side,
Quanomous, delay, responsiveness, robot-centric mode, parking, goal-distance and
goal-angle offsets, and diagnostic level. Runtime filter and menu state are not
persisted. Auto initialization clears required choices; missing choices warn and
fail clearly; alliance/side changes reset Pedro's starting and current pose.

The season subsystem API is intentionally observable because autonomous routines,
Panels, and tests compose it:

- `Drive` exposes mutable tuning/power/tolerance values, controller and timer
  seams, live driver transforms, centric/heading suppliers, stillness and target
  predicates, correction calculations, wait helpers, and the named autonomous
  route vocabulary.
- `Nav` exposes robot dimensions, season poses, parking/deposit/artifact choices,
  alliance-relative approaches, remaining artifact movement, and goal offsets.
- `Auto` validates Quanomous immediately and exposes the season command compiler,
  cycles, match-time wrappers, parking gate, and complete stop behavior.
- Conveyor, Deflector, Flywheel, Gate, Intake, and Kickstand expose their hardware,
  Panels tuning values, short qualified commands, immediate lifecycle stop, and
  their documented readiness/sensor behavior.
- `Timing` exposes replaceable timers and rumble state. `Lights` exposes its
  driver, color, configuration, duplicate suppression, and priority policy.
- `Vision` exposes the documented camera geometry, pipelines, timers, device,
  results, poses, artifact collections, processing operations, lock behavior, and
  selection/reset commands as integration and test seams.

These public surfaces are requirements, not a recommendation to expose unrelated
implementation details. New public members should be added only when a real
consumer, configuration surface, or test seam needs them.
