# Architectural Decisions

These short decision records preserve the alternatives considered and the reason
the current direction is defensible.

## Documentation Is Repository-Owned Markdown

**Decision:** Keep canonical documentation in `docs/`, publish it with MkDocs
Material and GitHub Pages, and use the root README as the landing page.

**Why:** Documentation changes receive the same review, branch, tag, and history as
code. MkDocs adds navigation and search without copying content. A GitHub wiki would
create separate history and can drift; if enabled, it should link to the canonical
site.

## Reuse Is Separated From Season Policy

**Decision:** Use `3drdNextFTC` as the reusable module, with TeamCode as the
consumer/policy layer.

**Why:** Multiple teams/seasons can share fixes without inheriting a field model or
hardware. Optional strategy systems remain in the robot repository until they are
deliberately adopted as part of the Quickstart contract.

## Reflection Is Allowed at Initialization Boundaries

**Decision:** Use reflection for subsystem discovery, hardware-field discovery,
setting metadata, and one-time diagnostics field lookup.

**Why:** These remove error-prone registries and duplicated menu declarations. The
work occurs during initialization or is cached, not in the high-frequency loop, and
has exhaustive tests.

Subsystem class discovery intentionally repeats for each OpMode lifecycle. This is
still initialization-bound work, and it prevents a Sloth hot reload from retaining
singleton instances created by the previous TeamCode class loader. Discovery loads
the classes through the active OpMode so Panels and the subsystem lifecycle share
the same live TeamCode class identity.

## Sloth Is Development Acceleration, Not Robot Architecture

**Decision:** Integrate Dairy Sloth at the TeamCode application boundary and expose
two named workflows: **Deploy Sloth** for ordinary TeamCode iteration and **Deploy
Full** for a clean APK install. Full deployment automatically removes the remote
Sloth overlay.

**Why:** Hot reload shortens the edit/test loop without moving Sloth into reusable
robot APIs. A clean full deployment remains the authoritative integration and
competition baseline because dependency, manifest, resource, and library changes
cannot be proven by replacing TeamCode bytecode alone.

Every shared Sloth entry point runs Gradle offline. Sloth is used only after a full
installation has primed the computer, so repository access adds delay and failure
risk without providing a valid Sloth workflow. Missing cached artifacts instead fail
promptly and identify that the computer needs an online full build.

## Probe ADB Before Refreshing Its Transport

**Decision:** Before transferring code, both deployment workflows perform a bounded
ADB shell round trip. A healthy connection is preserved. A failed or timed-out
probe triggers a disconnect and reconnect of only the configured Robot Controller,
followed by another round-trip verification.

**Why:** ADB's recorded transport state can outlive a physical controller power or
network interruption, so a state query alone is not sufficient proof that the
robot responds. An unconditional disconnect/reconnect reliably clears that stale
transport but needlessly interrupts healthy Logcat sessions and adds delay to every
Sloth deployment. The bounded probe preserves the fast healthy path while retaining
the teams' proven disconnect-before-connect recovery behavior.

## Controls Activate at Teleop Start

**Decision:** Separate `controls()` from `initialize()` and clear bindings for every
OpMode.

**Why:** Robot motion during init is unsafe, Auto must not receive manual controls,
and global binding accumulation caused double actions after re-init. Config menu
bindings remain an intentional init-time exception.

## Telemetry and Logging Are Separate

**Decision:** Keep `tel` for current state and `log` for events, with separate
levels/filters and a convenient shared config default.

**Why:** Driver/Panels state is replaced every frame; Logcat/history is cumulative.
Duplicating high-rate state into logs creates noise and cost.

## Hardware Update Hooks Are Split Between Library and TeamCode

**Decision:** Keep mutation-then-hook `update` in the reusable hardware API and
specific `tel()` fields/levels in TeamCode.

**Why:** Ordering and wrapper integration are universal; what students want to see
for a motor or sensor is a team policy.

## Commands Use Delegated Naming and Deferred Construction

**Decision:** Infer names for property-delegated instant/deferred commands and build
live-state command children at start.

**Why:** This removes repeated strings/log calls while preserving accurate pose,
config, and vision decisions and strict requirement ownership.

## Pedro Constants Are Neutral in Quickstart

**Decision:** Keep a structurally valid constants object in the seasonal base using
obvious template values and Pedro's drivetrain-encoder localizer. Dedicated
localizers such as Pinpoint remain documented, optional substitutions; introduce
Osiris tuning only in phase 3.

**Why:** The base must compile and demonstrate integration without making unsafe
claims about another robot or requiring a particular localization device. A new
season has one clear replacement point that still resembles Pedro documentation.

## Vendor GoBilda Code Remains Java

**Decision:** Import the supplied Prism implementation without a Kotlin rewrite.

**Why:** It is third-party code the team does not intend to maintain. Source fidelity
improves upstream comparison and replacement.

## Optional manual driver-command shaping

Place manual shaping after NextFTC's scalar and before PedroDriverControlled's
assist callbacks. A single vector tracker shares translation slew/jerk budgets;
turn uses a scalar S-curve. Preserve state on input/live tuning changes and rotate
both translation states on centric-frame changes. Keep timing internal, stops
immediate, and the default legacy integration unchanged. Robot-specific opt-in and
Panels settings belong to TeamCode. This does not add per-wheel current control or
a shared electrical cap. See REQ-PLT-048 and the driver-shaping architecture.

## Velocity profiles and current feedback utilities

Use NextControl's existing interpolator extension point for time-based exponential
and acceleration-limited velocity references. Keep sampled current feedback in a
separate post-controller power limiter. These season-neutral utilities belong in
`3drdNextFTC`; targets, gains, current thresholds, motor acquisition, and telemetry
remain with the owning robot subsystem. Clock injection allows deterministic tests.
See [Control](../architecture/control.md) for reset and integration contracts.

## Embedded Versus Robot-Level Control

**Decision:** On this exploration branch, replace Flywheel's Hub PIDF loop with two
NextControl velocity systems writing native NextFTC motor power. Keep the existing
distance/motion target model. Share mutable tuning parameters, not feedback history.

**Why:** The goal here is to explore idiomatic Kotlin and NextFTC control rather
than preserve the deployed controller. Separate systems keep each wheel's feedback
independent; a motor group would use only its leader's measurement. Normalized
velocity makes the example gains readable. Readiness uses a symmetric tolerance,
and zero target explicitly coasts rather than actively braking to zero.
This replaces the earlier embedded-control decision for this branch only.

Flywheel now applies the inherited acceleration-limited interpolator and sampled
current limiter independently per motor. Profiles shape the velocity reference;
limiters cap the resulting power using measured amperes. Keep readiness tied to the
final target and bypass both for immediate zero-power stop. New profile/current
settings are captured by suppliers and read by the existing instances on update;
PID/FF coefficient objects remain live. Panels writes fields directly, so suppliers
avoid reliance on Kotlin setters or per-cycle property copying. Profile progress
and current caps survive edits.
This demonstrates composition of NextControl utilities rather than a guaranteed
electrical protection mechanism (REQ-DEC-028).

Flywheel now chooses the inherited `SCurve` profile with live acceleration and jerk
suppliers (REQ-DEC-029), softening the onset and removal of acceleration as well as
bounding acceleration itself. The default spin-up reference is slower than the
linear ramp at the same acceleration ceiling. Current limiting and immediate
zero-power stop remain separate concerns.

## Separate Seasonal Upgrades from Framework Migrations

Upgrade FTC SDK 12, Panels, Sloth/Load, Pedro 2.1.2, Kotlin, Mockito, and drive
motor diagnostics in one review branch while retaining NextFTC v1 and upstream
drive-encoder localization. Gradle 9.1.0 follows the SDK baseline and supplies
the API required by Load 0.3.2. Keep Pedro 3 and NextFTC v2 in a later track.
Preserve shared history; eventual upgrade commits append to it.

## Team handbook and drivetrain onboarding

Make Get started the neutral entry point, provide editable Our robot pages, and
retain the complete inherited architecture and reconstruction contract in Platform
reference. Teams promote their robot overview to the home page as it matures.
A repository-local documentation skill helps connect team requirements to code,
tests, and measured evidence without inventing design decisions.

Keep the neutral mecanum scaffold unchanged. Offer the earlier paired-slot tank
recipe as an explicit TeamCode specialization reviewed against Pedro 2.1.2.
Require zero-strafe robot-centric Teleop and calibrated drive-encoder factors;
deduplicate telemetry for repeated motor references. Desktop tests establish the
adapter math and same-side outputs, not differential autonomous capability or
physical readiness. Dedicated odometry remains a separate physical validation.

## Implementable robot requirements

Treat requirement readiness as another student being able to implement and test
behavior without inventing policy. Keep observable behavior separate from chosen
code structure. Review lifecycle, event priority, failure outcomes, and subsystem
interactions explicitly. Use a hypothetical worked example to teach the process;
its values and behavior do not become a team's defaults. Track unresolved behavior
choices separately from physical calibration and never equate software readiness
with robot validation or a guaranteed one-shot build.

## Shared documentation preview

Use thin Windows/Linux launchers with one Python environment setup implementation.
A Gradle Exec task supplies a cross-platform Android Studio run configuration
without requiring a Python or shell IDE plugin. Install the pinned documentation
requirements only on initial setup or when their fingerprint changes.

## Native NextFTC hardware

NextFTC 1.1.0 marks MotorEx and IMUEx final. Use type aliases, lazy configuration
factories, and Kotlin extensions instead of composition that exposes only a
selected interface. Lifecycle discovery recognizes native instances, preserving
automatic initialization and subsystem fault isolation without requiring them
to implement our Hardware marker. Use the same pattern for servos so fluent
returns remain discoverable and students have one consistent declaration style.
Device keeps its existing SDK-device delegate. Avoid a vendor fork or bytecode changes.
