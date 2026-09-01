# Osiris autonomous specification

This page is the normative reconstruction detail for `REQ-DEC-030`–`033`. It
defines the reusable Quanomous data boundary, Decode command vocabulary, and exact
top-level compositions. It does not prescribe a particular stored competition
program: programs are field strategy data loaded onto the Robot Controller.

## Reusable Quanomous data contract

`3drdQuanomous` MUST remain independent of NextFTC. Its public behavior is:

- store JSON programs in `/sdcard/FIRST/quanomous`;
- list case-insensitive `.json` files sorted by filename;
- load/save a program as a JSON array;
- decode QR input as Android Base64 `NO_WRAP`, then GZIP UTF-8 JSON;
- parse the result as a JSON array;
- canonicalize JSON, SHA-256 hash it, and use the first eight lowercase hex digits
  for duplicate-content detection;
- return the previous name without another write when the same hash repeats;
- otherwise generate `MM-dd-HHmm--NNNN.json`, where N is stored-file count + 1;
- compile each object by its required string `cmd` member;
- reject an unregistered command with `Unknown Quanomous command: <name>`; and
- keep the compiler generic in output type so the module has no NextFTC dependency.

The Decode integration creates a `QuanomousCompiler<Command>`; it is this TeamCode
layer, not the reusable module, that understands robot commands and units.

## Decode JSON vocabulary

Every program is a JSON array of step objects. Property names are case-sensitive;
axial/lateral values are matched case-insensitively.

| `cmd` | Required fields | Optional fields/defaults | Compiled behavior |
|---|---|---|---|
| `delay` | `seconds: number` | none | NextFTC delay in seconds |
| `intake` | `spike: integer` | none | remaining-time-wrapped spike intake |
| `intake_gate` | none | none | remaining-time-wrapped gate intake |
| `deposit` | `locale: string`, `txo: number`, `tyo: number` | none | locale `near` means SOUTH, anything else NORTH; offsets are tiles |
| `release` | none | none | remaining-time-wrapped gate release |
| `chase` | `cycles: integer` | none | zero means `Int.MAX_VALUE`; otherwise exact count |
| `park` | none | `gate` from Config; `axial/lateral` = center | wait/drive/stop parking sequence |
| `drive` | `tx`, `ty`, `h` numbers | `axial/lateral` = center | curve to typed tile/degree pose; Y is `alliance(-abs(ty))` |

Axial text maps `front`, `back`, or otherwise center. Lateral maps `left`, `right`,
or otherwise center. `gate` uses its JSON boolean when present and Config's Park
Gate otherwise.

Example schema-valid program:

```json
[
  { "cmd": "intake", "spike": 1 },
  { "cmd": "deposit", "locale": "far", "txo": 0.0, "tyo": 0.0 },
  { "cmd": "chase", "cycles": 1 },
  { "cmd": "park", "gate": false, "axial": "back", "lateral": "right" }
]
```

This is a format example, not a promised competition strategy.

## Entry and time budget

Auto's timeout is `29.5 s`. `execute()` MUST compose in this order:

1. wait configured delay;
2. load and sequentially compile the explicitly selected Quanomous program;
3. end the routine after 29.5 seconds; and
4. execute stop-all.

There is no sample/fallback routine. A null or invalid selection is a critical
failure already prevented by Config's Auto start gate. The Auto OpMode schedules
this one entry command on Start.

`remaining(command)` MUST preserve every possible requirement declared by its
child and cap it to `max(0, 29.5 - current play seconds)` at execution time.

## Primitive compositions

Commands named below are subsystem-qualified whenever the verb could be ambiguous.

### Intake

`intakeStart()` first disables goal lock, then sequentially selects Intake forward,
Conveyor forward, and Gate close. The instant selections normally finish in the
same scheduler update, but their composition semantics remain ordered.

`intakeStop()` first enables goal lock and sequentially selects Flywheel forward,
Conveyor reverse, and Gate hold; waits `0.8 s`; then stops Conveyor and places
Intake on hold in sequence.

`intake(spike)` first performs `depositStop()`, then sequentially starts intake,
selects Drive intake power, and drives the selected spike path; finally waits
`0.2 s`.

### Deposit

`depositStart()` enables goal lock, then sequentially opens Gate, runs Intake
forward, selects fixed Flywheel forward, and selects Conveyor's distance-based
launch velocity.

`depositStop()` disables goal lock, then sequentially stops Conveyor and Flywheel,
resets Intake artifact state, and stops Intake.

`deposit(side, axial=0 in, lateral=0 in)` MUST be deferred and declare Drive,
Intake, Conveyor, Flywheel, Gate, and Vision requirements. At execution it chooses
a trigger distance:

- north: `-9 in`;
- south with follower X below -2 tiles: `-24 in`;
- other south: `-48 in`.

It runs `intakeStop()`, switches Auto drive power, and follows the corresponding
deposit path alongside this trigger sequence:

1. wait for the path-distance trigger;
2. wait until goal heading is within `22°` when X > 2 tiles, otherwise `15°`;
3. for north only, wait until follower is not busy;
4. for north only, wait up to one second for heading within `4°`;
5. for north only, wait up to one second for Flywheel ready;
6. sequentially open Gate and run Intake/Flywheel/Conveyor launch; and
7. wait `0.4 s`.

`releaseGate()` drives the gate path alongside Gate close. `gateIntake()` starts
intake, drives Gate, switches high, drives gate-intake for at most 1.5 seconds,
waits 1.5 seconds, waits for Intake full for at most 2 seconds, switches low, drives
the departure for at most 0.4 seconds, then restores Auto power.

### Locks and direct drive

`goalLock(enabled)` coordinates the matching Vision goal command alongside the
matching Drive goal command. `drive(pose)` performs `depositStop()` then a curve to
the supplied pose.

### Chase

`chase(cycles)` resets Intake then repeats `chaseCycle()` exactly `cycles` times.
A cycle races up to four repeated chase-intake attempts against chase completion,
then deposits.

A chase-intake attempt drives to scan alongside enabling Vision chase and waiting
up to two seconds for an element. It then sequentially selects Vision backup,
starts intake, enables Drive chase, and runs the chase-controlled drive. That drive
races Intake's next-element wait, 0.4 seconds of stillness, and at-element wait;
Vision then removes/resets the selected observation.

Chase completion races Intake full against either two artifacts or one artifact
followed by reaching the north-deposit distance threshold of `-0.75 tiles`.
Chase deposit unlocks Drive chase, deposits on configured side, waits `0.8 s`, and
performs `depositStop()`.

### Park and shutdown

Parking waits up to `0.8 s` for goal lock to be false, switches Auto power, follows
the selected parking path/alignment, then stop-all. `canPark()` is exactly
`!Drive.goalLocked`.

Stop-all disables both goal locks and then sequentially runs Drive chase unlock,
Drive stop, Intake stop, Conveyor stop, Gate close, and Flywheel stop. Lifecycle stop
hooks on powered subsystems remain independently required because command cleanup
is not guaranteed after every abnormal OpMode termination.

## Acceptance

Tests MUST compile every JSON command and every optional/default branch; reject
unknown commands and missing critical data; assert top-level ordering, timeout,
requirements, races/repetitions, deferred evaluation, parking gate, and stop-all.
Before field use, every stored program MUST be decoded, dry-run with mechanisms
made safe, checked against the 29.5-second budget, and then tested at reduced power.
