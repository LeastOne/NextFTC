# Verification

Verification has three layers: fast focused tests while editing, mandatory full
repository verification before completion, and physical robot validation for
hardware behavior.

## Mandatory command

From the repository root on Windows:

```powershell
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :3drdQuanomous:check :3drdQuanomous:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

On a POSIX runner use `./gradlew` with the same tasks.

This verifies reusable-library tests, independent Quanomous tests, TeamCode tests,
100% line and branch thresholds, lint/check tasks, dependency compatibility, and
debug APK assembly. Do not weaken a threshold to make the command pass.

## Reusable endpoint

Validate the tag independently in a detached worktree so current work is untouched:

```powershell
git worktree add ..\NextFTC-season-base reusable-season-base
Push-Location ..\NextFTC-season-base
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :3drdQuanomous:check :3drdQuanomous:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
Pop-Location
```

Remove the worktree only after checking the resolved absolute path:

```powershell
git worktree remove ..\NextFTC-season-base
```

## Documentation

Create an isolated environment and build in strict mode:

```powershell
python -m venv .venv-docs
.\.venv-docs\Scripts\python -m pip install -r requirements-docs.txt
.\.venv-docs\Scripts\mkdocs build --strict
```

Strict mode fails on navigation omissions and invalid internal documentation links.
The generated `site/` directory is disposable and must not be committed.

## Focused test workflow

During implementation, run the narrowest relevant module/test task first, then the
mandatory command. Each production subsystem has a dedicated test class. Shared
test harnesses provide mocked FTC hardware by configured name and reset global
NextFTC/Panels state between tests.

## Physical validation

Unit tests can prove signs, values, and calls against mocks. They cannot prove:

- a hardware-map name matches the configured Control Hub;
- a motor/servo is physically mounted in the assumed direction;
- PID/constraints are safe and tuned;
- a sensor sees the real field/environment;
- network deployment, Driver Station, Panels, and robot firmware interoperate.

Treat the hardware checklist on [Phase 3](phase-3.md) as mandatory evidence before
competition use.
