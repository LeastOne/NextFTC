# Clean-room reconstruction audit

The documentation was audited on 2026-08-30 by a fresh-context agent in an
isolated worktree beginning at FTC foundation commit `26cd1fdd`. The agent could
read the canonical documentation and upstream references but could not inspect
later production source, diffs, or implementation history.

## Independent prose-only result

The agent first rebuilt both phases using only the prose requirements and wrote
its own meaningful tests.

| Endpoint | Commit | Result |
|---|---|---|
| reusable seasonal base | `8a5609bad536774124b501f9864c94a048691e80` | mandated Gradle verification passed; 80 tests; 100% line and branch coverage |
| complete Decode/Osiris robot | `298a76eff996bd0fe8e32a8f317b7adb611ce8cf` | mandated Gradle verification passed; 112 tests; 100% line and branch coverage; strict MkDocs passed |

The independent final endpoint is protected locally by tag
`cleanroom-final-20260830`; the reusable endpoint is protected by
`cleanroom-reusable-season-base-20260830`. No physical-hardware result was
inferred from unit tests.

This proves the documentation can produce a compilable, behaviorally complete,
fully tested implementation without replaying Git history.

## Withheld-test acceptance

Only after freezing the independent result were the repository's existing tests
revealed. They exposed source-level details that prose had not fixed: exact package
locations, overloads, property visibility, qualified command names, and replaceable
storage/clock/decoder/device seams.

Those contracts were recorded in [Public API contract](../reference/public-api-contract.md)
and applied without consulting the production implementation. The adapted audit
endpoint is `43ca16913bc76dd72a24d2f81e2ec57c8d7f5fe2`.

| Hidden suite | Result |
|---|---:|
| `3drdNextFTC` | 123 / 123 passed |
| `3drdQuanomous` | 4 / 4 passed |
| `TeamCode` | 123 / 123 passed |
| **Total** | **250 / 250 passed** |

All module checks and `TeamCode:assembleDebug` passed. The exact aggregate command
failed only its coverage verifications because the independent implementation
contained additional internal paths not exercised by the canonical tests:

| Module | line | branch | required |
|---|---:|---:|---:|
| `3drdNextFTC` | 90% | 80% | 100% / 100% |
| `3drdQuanomous` | 90% | 60% | 100% / 100% |
| `TeamCode` | 90% | 70% | 100% / 100% |

The thresholds were not weakened. This does **not** indicate an acceptance
behavior failure: all 250 withheld tests passed. It shows that source coverage is
implementation-relative. A clean-room implementation with extra defensive or
compatibility branches must add tests for those branches or remove them before it
can satisfy the repository's coverage policy.

## Completeness conclusion

For practical reconstruction, the documentation now reaches the useful 100%
standard:

- phase 2 independently produces a usable neutral seasonal base;
- phase 3 independently produces the complete Decode/Osiris behavior;
- the withheld canonical suites compile and pass without production-source access;
- exact public package and API contracts are now written down; and
- physical-only claims remain explicitly pending.

“100%” does not mean byte-identical private implementation, identical test design,
or proof of unperformed robot checks. Requiring those would turn the documentation
into a disguised source-code transcript and would not improve a team's ability to
start a season or rebuild the robot.

## Remaining physical validation

The audit cannot prove motor direction, hardware-map correctness, safe tuning,
camera geometry, real QR acquisition, Driver Station/Panels networking, or full
autonomous paths on Osiris. Complete the [hardware worksheet](../guides/hardware-worksheet.md)
and Phase 3 physical checklist before treating the rebuilt robot as competition
ready.
