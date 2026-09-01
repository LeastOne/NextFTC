# Osiris navigation and vision specification

This page refines `REQ-DEC-002`, `REQ-DEC-012`–`014`, and the navigation portions
of `REQ-DEC-030`–`033`. Distances in the pose catalog are 24-inch tiles unless
explicitly marked inches; headings are degrees and converted through typed units.

## Coordinate contract

Pedro pose is the canonical robot coordinate system: +X right on its standard
field drawing, +Y up, heading zero along +X, and positive rotation
counterclockwise. Panels MUST draw that pose directly without a fabricated
rotation/translation. FTC or Limelight representations MUST be converted at their
boundary.

Alliance mirrors Y/headings through red `-1`, blue `+1`. Side mirrors X choices
through north `+1`, south `-1`. One tile is exactly 24 inches. Public distances,
angles, and path progress use the reusable typed units.

## Named poses

All dynamic properties evaluate current config when read.

| Name | X | Y | Heading/notes |
|---|---:|---:|---|
| Start, unknown selection | `0` | `0` | `0°` |
| Start, north | `2.7` | `alliance(-0.8)` | default `0°` |
| Start, south | `-2.7` | `alliance(-0.8)` | default `0°` |
| Spike 0 | `2.1` | red: `alliance(-2.75)`; blue: `alliance(-2.65)` | `alliance(-15°)` |
| Spike 1 | `1.5` | `alliance(-1.1)` | `alliance(-90°)` |
| Spike 2 | `0.5` | `alliance(-1.1)` | `alliance(-90°)` |
| Spike 3 | `-0.55` | `alliance(-1.1)` | `alliance(-90°)` |
| Score | `1.0` | `0.5` | `90°` |
| Park sample | `1.5` | `0` | `0°` |
| Gate | north `0.15`, south `-0.15` | `alliance(-2)` | `alliance(-90°)` |
| Gate intake | `0.65` | `alliance(-2.65)` | `alliance(-135°)` |
| Gate depart | `0.25` | `alliance(-2.65)` | `alliance(220°)` |
| Goal | `-2.75` | `alliance(-2.75)` | `alliance(45°)` |
| Chase scan | `2.25` | `alliance(-0.75)` | `alliance(-85°)` |
| Base | `1.5` | `alliance(1.33)` | `0°` |

## Deposit and parking targets

South deposit starts at `(-1, alliance(-0.75))` tiles and faces Goal. Its facing
offset is alliance-mirrored `-182°` when follower X is below -2 tiles, `-170°` when
above +2 tiles, and `-175°` otherwise. Apply caller axial/lateral offsets, then face
Goal again so translation cannot leave a stale heading.

North deposit starts at `(2.3, alliance(-0.6))` tiles. Its face offset is
alliance-mirrored `-177°` on red or `178°` on blue. Apply caller offsets and face
again.

Parking accepts `gate`, axial alignment, and lateral alignment. Gate parking is
`(0, alliance(-1.75), alliance(-90°))`. Non-gate parking X is side-mirrored `2.6`
tiles for north or `2.4` for south; Y is `alliance(-1.75)` for north or
`alliance(-1.0)` for south; heading is `90° + side(90°)`. The reusable Nav pose
builder applies robot length/width for requested alignment.

## Artifact and goal calculations

Chase scan execution `n` uses Vision's element X if present, otherwise
`2.75 - (n mod 3) * 0.75` tiles, with Y `alliance(-2.4)` tiles and heading
`alliance(-90°)`.

The commanded artifact pose uses a stable backup when no element exists:

```text
x = 3 tiles - ELEMENT_RADIUS * 5
y = alliance(ELEMENT_RADIUS - 3 tiles)
```

With an element, clamp element X between
`2 tiles + 2 inches + robotWidth/2` and `3 tiles - robotWidth/2`; use the same Y
formula and `alliance(-90°)` heading.

Remaining corrections are follower X minus artifact X, follower Y minus approach
Y, and normalized follower heading minus artifact heading. Approach Y equals final
artifact Y inside one element radius. Otherwise use a staging Y three element
radii alliance-ward; remain clear of half the robot length and avoid backing away
from a better current approach.

Goal distance uses Limelight `botpose` when available, otherwise follower pose,
then adds the north offset above X=1 tile or the south offset otherwise. Goal
heading similarly faces the goal from botpose/follower pose, applies the matching
configured angle offset, and returns normalized follower-minus-target heading.

## Path catalog

Every named path MUST be deferred so follower pose, config, and vision are read at
execution. Unless noted, paths use the reusable curved-path helper and default
heading interpolation. `A(n)` and `L(n)` below mean axial/lateral translation by
the typed distance `n`; `alliance(n)` and `side(n)` mirror the numeric tile value.

| Command | Exact curve/control contract |
|---|---|
| `toSpike(index)` | Dispatch 0–3; reject anything else with `Unknown spike: <index>`. |
| `toSpike0` when current X > 1 tile | `spike0.A(1 tile).L(alliance(0.8) tiles)` → `spike0.A(-1 tile).L(alliance(-0.2) tiles)` → `spike0.A(0.45 tiles)`; cap 3 s. |
| `toSpike0` otherwise | `spike0.A(-3.25 tiles).L(alliance(2.15) tiles)` → `spike0.A(-1.5 tiles).L(alliance(-0.5) tiles)` → `spike0.A(0.45 tiles)`; cap 3 s. |
| `toSpike1` | First point starts `spike1.A(-0.9 tiles)` when X > 1 tile, otherwise `A(-1.85 tiles)`; additionally `A(-0.75 tiles)` when `abs(Y) > 1.75 tiles`; additionally `L(alliance(0.3) tiles)` when X ≤ 1 tile. End `spike1.A(1.4 tiles)`; do not hold. |
| `toSpike2`, `abs(Y) < 1.75 tiles` | `spike2.A(-1.1 tiles).L(alliance(sign(X) * -0.3) tiles)` → `spike2.A(1.3 tiles)`; do not hold. |
| `toSpike2`, outer Y | `spike2.A(-0.4 tiles).L(alliance(-0.7) tiles)` → `spike2.A(-0.4 tiles).L(alliance(0.2) tiles)` → `spike2.A(1.3 tiles)`; do not hold. |
| `toSpike3`, `abs(Y) < 1.75 tiles` | `spike3.A(-1.5 tiles).L(alliance(sign(X) * -0.2) tiles)` → `spike3.A(1 tile)`; do not hold. |
| `toSpike3`, outer Y | `spike3.A(-0.4 tiles).L(alliance(0.7) tiles)` → `spike3.A(-0.4 tiles).L(alliance(-0.2) tiles)` → `spike3.A(1 tile)`; do not hold. |
| `toDeposit(NORTH, offsets)` | Select medium, curve to `depositNorth(offsets)`, then select high. |
| `toDeposit(SOUTH, offsets)`, current X > -1 tile | Curve through `Pose(currentX, alliance(-0.5))` measured in raw Pedro inches, then `depositSouth(offsets)`. |
| `toDeposit(SOUTH, offsets)`, otherwise | Curve through midpoint of current pose and `depositSouth(offsets)`, then the target. |
| `toGate` | `gate.A(-1.75 tiles)` → gate; do not hold. |
| `toGateIntake` | `gateIntake.A(-0.1 tiles).L(alliance(-0.3) tiles)` → gateIntake. |
| `toGateIntakeDepart` | `gateIntakeDepart.A(side(-0.05) tiles).L(-0.1 tiles)` → gateIntakeDepart; do not hold. |
| `toBase`, `toChaseScan` | Curve directly to their named pose. |
| `toParking(gate, axial, lateral)` | Curve to the live `parking(...)` pose. |
| `toChase(count)` | `chase(count).A(-1.25 tiles)` → `chase(count)`. |
| `toScore` | `score.A(-0.5 tiles).L(0.25 tiles)` → score. |
| `toPark` | Curve directly to the sample Park pose. |

The implementation tests MUST assert the exact generated control points and
hold/power choices. These details are strategy behavior, not reusable pathing APIs.

## Vision hardware and tuning

| Value | Required default |
|---|---:|
| Limelight name | `limelight` |
| Turret servo name | `turret` |
| Inches per meter | `39.3701` |
| Camera upside-down | `true` |
| Camera X/Y/Z | `3.93701`, `-0.3937008`, `16.14173 in` |
| Camera pitch/yaw | `-0.75°`, `1.15°` |
| Element radius | `2.5 in` |
| Elevation/X-bearing/Y-bearing scalars | `1.0`, `1.0`, `1.0` |
| Turret goal/chase position | `0.10`, `0.83` |
| Turret scaled range | `0.10..0.85` |
| Initial logical turret position | `1.0` |
| Pipeline settle time | `0.6 s` |

The Limelight hardware initializer switches to pipeline 0 and starts the camera.
The turret uses the scaled range above. Initialization restores QR mode, logical
position 1.0, clears current result/botpose/element and both artifact lists, and
resets the settle timer. Stop MUST call `limelight.stop()`.

## Pipeline contract

| Pipeline | Index | Purpose |
|---|---:|---|
| QR code | `0` | load/store a Quanomous program |
| AprilTag | `1` | estimate robot pose for goal assist |
| Green | `2` | locate green artifacts |
| Purple | `3` | locate purple artifacts |

Periodic MUST first report a connection error and return when disconnected.
Otherwise it sends follower heading in degrees to Limelight, captures latest
result, clears botpose, and updates the turret. A valid result is processed only
after the settle time. INFO reports pipeline; DEBUG reports validity plus optional
robot/element pose with fixed precision.

Goal lock only acts when enabling during Teleop: it selects AprilTag and turret
position `0.10`. Chase lock only acts when enabling: it selects purple, clears the
selected element, and sets `0.83`. Switching to either color pipeline also sets
chase position and always resets the settle timer.

### QR processing

For every barcode result, decode/store it through reusable Quanomous, select the
returned program name in config, notify `ConfigComponent.changed()`, and INFO-log
family plus data. Duplicate decoded content is deduplicated by Quanomous hashing.

### AprilTag processing

Use only the first fiducial. Blue accepts ID 20; red accepts ID 24; all mismatches
are ignored. Convert MT2 pose X/Y meters with `39.3701` and preserve its yaw in
radians as the Pedro pose heading.

### Color projection and selection

Ignore color results before OpMode Start. For each result, negate both target
angles when the camera is upside-down, then project:

```text
height    = cameraZ - elementRadius / 2
elevation = radians(cameraPitch + targetPitch)
bearing   = radians(cameraYaw - targetYaw)
distance  = abs(height / tan(elevation * elevationScalar))
localX    = cameraX + distance * cos(bearing * bearingXScalar)
localY    = cameraY + distance * sin(bearing * bearingYScalar)
```

Transform that local axial/lateral offset from current follower pose and give it a
heading `atan2(localY, localX)`. Remove same-color observations within one element
radius before adding the new pose. Choose the closest pose across primary and
secondary color lists; retain the previous selected element if both are empty.

Reset removes observations of both colors within one radius of the selected
element, then clears it. Backup selects `(2.5 tiles, alliance(-2.9 tiles))` only
when no element exists. `waitForElement()` waits for a non-null selection.

## Acceptance

Desktop tests MUST cover every pose/config branch, path entry, conversion, pipeline,
lock, filtering, reset, hardware lifecycle, and telemetry branch. Physical tests
MUST confirm field axes, AprilTag IDs, camera orientation/extrinsics, element
projection, turret endpoints, pose/drawing agreement, and Limelight shutdown.
