# Verification

Verification has three layers: fast focused tests while editing, mandatory full
repository verification before completion, and physical robot validation for
hardware behavior.

## Mandatory Command

From the repository root on Windows:

```powershell
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :3drdQuanomous:check :3drdQuanomous:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

On a POSIX runner use `./gradlew` with the same tasks.

To generate and open only TeamCode's coverage report, use `coverage.cmd` on
Windows or `./coverage.sh` on Linux. Both accept `run`, `open`, or `refresh`.

Verify portable ADB discovery without changing a robot connection:

```powershell
.\scripts\adb.cmd version
```

```bash
./scripts/adb.sh version
```

This verifies reusable-library tests, TeamCode tests, 100% line and branch
thresholds, lint/check tasks, dependency compatibility, and debug APK assembly. Do
not weaken a threshold to make the command pass.

Verify deployment wiring without contacting a robot:

```powershell
.\gradlew :TeamCode:deploySloth --dry-run
.\gradlew :TeamCode:installDebug --dry-run
```

Both plans must include `:TeamCode:ensureAdbConnection`; the full deployment must
also include `:TeamCode:removeSlothRemote`.

## Reusable Endpoint

Validate Quickstart `main` independently in a fresh clone so local state cannot
mask a missing file or dependency:

```powershell
git clone https://github.com/3DRoboticsDuluth/NextFTC-Quickstart.git ..\NextFTC-Quickstart-audit
Push-Location ..\NextFTC-Quickstart-audit
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
Pop-Location
```

Remove the audit clone only after checking its resolved absolute path.

## Documentation

Create an isolated environment and build in strict mode:

```powershell
python -m venv .venv-docs
.\.venv-docs\Scripts\python -m pip install -r requirements-docs.txt
.\.venv-docs\Scripts\mkdocs build --strict
```

Strict mode fails on navigation omissions and invalid internal documentation links.
The generated `site/` directory is disposable and must not be committed.

## Focused Test Workflow

During implementation, run the narrowest relevant module/test task first, then the
mandatory command. Each production subsystem has a dedicated test class. Shared
test harnesses provide mocked FTC hardware by configured name and reset global
NextFTC/Panels state between tests.

## Physical Validation

Unit tests can prove signs, values, and calls against mocks. They cannot prove:

- A hardware-map name matches the configured Control Hub;
- A motor/servo is physically mounted in the assumed direction;
- PID/constraints are safe and tuned;
- A sensor sees the real field/environment;
- Network deployment, Driver Station, Panels, and robot firmware interoperate.

Use the [hardware worksheet](../guides/hardware-worksheet.md) as mandatory evidence
before competition use.

## Native hardware adapter validation — October 24, 2026

The native MotorEx/IMUEx/ServoEx/CRServoEx adaptation (REQ-PLT-044) passes the full required Gradle
check/coverage/APK command with `--max-workers=2 --continue`. Debug and release
each pass 138 library tests and 28 TeamCode tests. Both modules retain 100% line
and branch coverage (library: 885 lines / 349 branches; TeamCode: 99 lines / 18
branches), with unchanged thresholds/exclusions. Strict MkDocs and diff checks pass.

Tests verify exact native runtime types and direct upstream APIs, lazy constructor
configuration without an active OpMode, original constructors and servo fluent returns, identity-deduplicated
initialization, failure isolation and retry, and telemetry/update behavior.
No robot deployment or physical test was performed. Consuming teams must rebuild
and perform a full install for the changed runtime types before using Sloth.

Treat the hardware checklist on [Phase 3](phase-3.md) as mandatory evidence before
competition use.

## Osiris native-hardware trial rebase — October 24, 2026

The personal NextFTC main branch was rebased locally onto Quickstart's unpublished
native-hardware adaptation and documentation launchers. Robot declarations and
behavior were preserved. Conveyor and Intake required explicit imports for the
new `velocityPercentage` extension; no control logic changed. The verification
guide retained both the platform validation and Phase 3 hardware checklist.

The full three-module Gradle check/coverage/APK command passes with
`--max-workers=2 --continue`. Debug and release each pass 138 NextFTC library tests,
4 Quanomous tests, and 129 TeamCode tests. All modules retain 100% line and branch
coverage: NextFTC 885/349, Quanomous 50/4, TeamCode 899/349 (lines/branches).
Strict MkDocs and diff checks pass. No thresholds or exclusions were relaxed.

This remains desktop evidence. A full APK installation and physical verification
of mechanism directions, initialized configuration, telemetry, and lifecycle Stop
are still needed before competition use. The trial does not establish robot
hardware validation or authorize deployment.
