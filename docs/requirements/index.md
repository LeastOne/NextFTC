# Requirements

Requirements are organized by the layer in which they become valid. This mirrors
the history and prevents robot-specific decisions from leaking into the reusable
platform.

| Prefix | Layer | Scope |
|---|---|---|
| `REQ-FND` | FTC foundation | Build, toolchain, warning, and development-tool changes immediately above upstream FTC |
| `REQ-PLT` | Reusable platform | Season-neutral libraries, lifecycle, commands, diagnostics, hardware, configuration, pathing, and tests |
| `REQ-SCF` | Seasonal scaffold | The minimum neutral `TeamCode` application that proves the platform can launch a season |
| `REQ-DEC` | Decode/Osiris | Robot hardware, field model, controls, vision, mechanisms, and autonomous behavior |
| `REQ-QLT` | Quality | Verification, coverage, documentation, and history rules applying across owned code |

## Language

- **MUST** is mandatory and must have an acceptance path.
- **SHOULD** is the established default; deviations require a recorded reason.
- **MAY** is supported but optional.

The requirement is the durable contract. A listed implementation is the current
way the contract is met, not permission to bypass the contract when refactoring.

## Global quality requirements

| ID | Requirement | Why | Verification |
|---|---|---|---|
| `REQ-QLT-001` | Owned production code MUST maintain 100% line and branch coverage. | Robot code changes rapidly; exhaustive executable examples keep reuse safe. | Run all three `unitTestCoverage` tasks. |
| `REQ-QLT-002` | Tests MUST live with the module and package of the behavior they cover. | Proximity makes examples discoverable and keeps library boundaries honest. | Inspect test packages and Gradle source sets. |
| `REQ-QLT-003` | Tests MUST verify behavior and externally relevant state, not private implementation trivia. | Refactoring should not require needless test rewrites. | Review tests with the associated behavior. |
| `REQ-QLT-004` | The complete verification command MUST pass before published changes are considered complete. | Unit success alone does not prove Android assembly or cross-module compatibility. | Follow [Verification](../rebuild/verification.md). |
| `REQ-QLT-005` | Reusable code MUST remain independent of Decode and Osiris policy. | A season base is useful only if it can compile without last season's robot. | Check out `reusable-season-base` and run the full verification command. |
| `REQ-QLT-006` | Requirements, architecture, rebuild guidance, and implementation MUST evolve together. | Documentation must remain capable of reconstructing the system. | Build docs with `mkdocs build --strict`; review traceability. |
| `REQ-QLT-007` | Tests and fixes SHOULD be folded into the conceptual commit they complete. | History should teach the architecture in dependency order. | Review `git log --reverse` and changed paths. |
| `REQ-QLT-008` | Documentation MUST support both a first robot built from the reusable endpoint and a clean-room reconstruction of the completed robot without later production source. | A reusable platform and a completed robot require different forms of transfer knowledge. | Follow the first-robot guide and run the one-shot audit protocol; record all ambiguities. |

Continue with the requirements for the [FTC foundation](foundation.md),
[reusable platform](platform.md), [seasonal scaffold](seasonal-base.md), and
[Decode/Osiris implementation](decode-osiris.md).

The Decode index is refined by separate normative specifications for the
[system](osiris-system.md), [mechanisms](osiris-mechanisms.md),
[navigation and vision](osiris-navigation-vision.md), and
[autonomous behavior](osiris-autonomous.md). These exact tables and algorithms are
required when reconstructing without access to the implementation.
