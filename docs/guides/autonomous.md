# Build autonomous behavior

Autonomous is built in three layers: mechanism commands, coordinating Auto methods,
and optional Quanomous strategy data.

## Start with subsystem commands

A mechanism should expose meaningful state changes such as `Gate.open`,
`Intake.forward`, or `Drive.toDeposit(...)`. The subsystem remains responsible for
its own state and hardware; Auto should not assign its fields directly.

Use the qualified subsystem reference when common verbs would otherwise be
ambiguous:

```kotlin
Gate.open
Intake.stop
Flywheel.forward
```

## Compose intentions

```kotlin
fun depositStart() = goalLock(true).then(
    Gate.open,
    Intake.forward,
    Flywheel.forward,
    Conveyor.launch
)
```

Put top-level entry methods near the top of the Auto subsystem, followed by the
operations they compose. This top-down order lets a student read from overall
routine to detail.

## Defer live decisions

Use a deferred wrapper when path creation or branching reads follower pose,
alliance, side, vision, or mutable config. Declare every possible subsystem
requirement on the wrapper.

```kotlin
fun deposit(side: Side) = deferred(
    Drive, Intake, Conveyor, Flywheel, Gate, Vision
) {
    // Build the child from current state.
}
```

## Use units to explain waits

Prefer typed overloads over named-double methods:

```kotlin
Drive.until((-9).inches)
Drive.until(50.pct)
Drive.until(50.pctT)
```

Use explicit timeouts for sensor/follower waits that could otherwise block the
entire routine.

## Enforce one top-level time budget

Decode waits the configured delay, executes the selected program, interrupts it at
29.5 seconds, then stops all mechanisms. A helper may calculate the remaining
overall time for a step. Do not allow nested behavior to reset the match budget.

## Quanomous handlers

Keep parsing/storage generic. TeamCode's compiler map owns the season vocabulary and
translates JSON into typed command compositions. Validate every supported step and
fail loudly for missing programs or required values.

## Test progression

1. Unit test each mechanism command.
2. Unit test every coordinating Auto method and requirement set.
3. Unit test every Quanomous handler and default.
4. Verify Auto refuses missing setup selections.
5. Dry-run with outputs safe.
6. Run one path/mechanism segment at a time.
7. Run the complete strategy at reduced power where possible.
8. Confirm timeout and stop-all behavior under interruption.
