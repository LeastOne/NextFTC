# Rebuild the repository

This guide supports two reconstruction modes:

1. **Exact replay** uses the conceptual commits to reproduce the current tree.
2. **Clean implementation** uses requirements and acceptance criteria to recreate
   the system when commits cannot be reused or dependencies have advanced.

Both begin from the official FTC foundation and stop at explicit phase gates.

## Prerequisites

- Windows, Git, and Android Studio with the Android SDK
- JDK resolved by the committed Gradle daemon toolchain
- Network access to Google, Maven Central, Panels, Pedro, and NextFTC repositories
- Git identity `3drdProgramming <programming@3droboticsduluth.com>` for canonical
  repository maintenance
- Optional: Python 3.13 for local documentation preview
- A Control Hub/Robot Controller and ADB for hardware validation

## Baseline

The local baseline is the FTC v11.2 upstream merge `26cd1fdd`. Verify it before
rebuilding:

```powershell
git show --no-patch --format="%H %s" 26cd1fdd
git branch rebuild/nextftc 26cd1fdd
git switch rebuild/nextftc
```

If recreating from FIRST's repository rather than this repository, fetch the FTC
v11.2 release history and identify the equivalent release merge. Do not begin from
a zip file; it loses the upstream ancestry this architecture requires.

## Phase gates

| Phase | End reference | Gate |
|---|---|---|
| 1 — FTC foundation | `e4d71312` | FTC project syncs/builds; ADB tools exist; only targeted warning policy changed |
| 2 — reusable platform | `reusable-season-base` | Full mandated verification passes with neutral TeamCode |
| 3 — Decode/Osiris | phase-3 implementation commits | Full verification passes and robot hardware tests are completed |

Follow the phase pages in order:

1. [Phase 1 — FTC foundation](phase-1.md)
2. [Phase 2 — reusable platform](phase-2.md)
3. [Phase 3 — Decode and Osiris](phase-3.md)
4. [Verification](verification.md)

## Exact replay

The commit tables on each phase page are in dependency order. To recreate exactly,
cherry-pick the listed commits in that order. To recreate conceptually, implement
each row's “what” and “why,” follow its linked architecture page, add the tests in
the same change, and pass the row's checkpoint.

## Reconstruction completion criteria

A rebuild is complete only when:

- the module dependency direction matches the architecture;
- the reusable endpoint compiles and passes coverage independently;
- no robot-specific values exist before phase 3;
- final `main` passes all tests, coverage verification, and Android assembly;
- documentation builds with strict mode;
- robot-side hardware and control checks are recorded for the physical robot.
