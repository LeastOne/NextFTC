# Quanomous

Quanomous separates autonomous strategy data from the Kotlin commands that execute
it. The unusual name is a 3DRD project term; it is intentionally included in the
repository dictionary.

## Reusable Module

`3drdQuanomous` owns three concerns:

- `Quanomous` parses program JSON and exposes available program names;
- `QuanomousStorage` obtains and stores program data through FTC storage;
- `QuanomousCompiler<T>` maps each JSON step name to a caller-provided function that
  returns `T`.

The generic `T` is the key independence point. The module knows nothing about
NextFTC `Command`; a caller can compile to another representation.

## TeamCode Integration

Decode's `Quanomous` subsystem creates `QuanomousCompiler<Command>` with handlers
for:

- `delay`
- `intake`
- `intake_gate`
- `deposit`
- `release`
- `chase`
- `park`
- `drive`

Handlers translate JSON values into typed units, field geometry, and composed
commands. Small JSON helpers apply defaults and convert axial/lateral text.

## Selection and Failure

The config setting uses the reusable Quanomous object as an options provider, so
the Driver Station menu lists stored program names without a TeamCode copy of the
list. Auto resets the selection during init and requires an explicit choice.

Loading failure is critical. The system does not fall back to a sample command,
because quietly running a different competition strategy is more dangerous than
refusing to start.

## Execution

The Auto OpMode schedules `Auto.execute()`. It waits the configured delay, loads and
compiles the selected program, caps the routine at 29.5 seconds, and runs `stopAll()`.
Individual handlers commonly wrap behavior in `Auto.remaining()` so a late-starting
step cannot exceed the overall timeout.

Deferred commands preserve live pose and target state while Quanomous preserves
data-driven strategy. The two solve different problems and are intentionally
composed rather than merged.
