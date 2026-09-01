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

This verifies reusable-library tests, TeamCode tests, 100% line and branch
thresholds, lint/check tasks, dependency compatibility, and debug APK assembly. Do
not weaken a threshold to make the command pass.

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

Treat the hardware checklist on [Phase 3](phase-3.md) as mandatory evidence before
competition use.

## Interpreting clean-room coverage

Coverage is measured against the implementation being tested. A clean-room rebuild
may pass every canonical behavior test yet report less than 100% if it introduces
additional branches or defensive paths. Do not lower the threshold. Add meaningful
tests for those paths or simplify the implementation, then rerun the mandatory
command. The [clean-room audit](clean-room-audit.md) records one such result so it
cannot be mistaken for a behavioral acceptance failure.
