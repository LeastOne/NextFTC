# One-shot clean-room rebuild

This protocol measures whether the documentation can reconstruct the project, not
whether Git can replay it. Use it after substantial architecture or robot changes
and before claiming the requirements are complete.

## Modes are deliberately different

| Mode | May use later commits/source? | Purpose |
|---|---|---|
| Exact replay | yes | Recover the known tree efficiently |
| Requirements rebuild | no | Produce an equivalent implementation from documented behavior |
| Documentation audit | no, and record every ambiguity | Find missing requirements |

A clean-room result need not have byte-identical source. It MUST have the same
observable behavior, module boundaries, safety properties, tests, and phase gates.

## Prepare an isolated worktree

Create it outside the active checkout so current implementation files are not
accidentally visible:

```powershell
git worktree add ..\NextFTC-cleanroom 26cd1fdd
Set-Location ..\NextFTC-cleanroom
git switch -c rebuild/documentation-audit
git config user.name 3drdProgramming
git config user.email programming@3droboticsduluth.com
```

Provide the agent a snapshot of `docs/`, `mkdocs.yml`, `AGENTS.md`,
`.github/CONTRIBUTING.md`, and the official upstream links. If acceptance tests are
provided, make them read-only and disclose that they are executable requirements.
Do not provide phase-2/phase-3 production source, diffs, or implementation commits.

For the strictest audit, initially withhold existing tests as well. Run them only
after the agent has implemented from prose; a failure then identifies either a
missing written contract or an incorrect implementation.

## Canonical one-shot instruction

Use this prompt as one uninterrupted agent objective:

> Starting from FTC Robot Controller v11.2 at the supplied foundation commit,
> reconstruct this repository from the canonical documentation. Implement phase 1,
> phase 2, and phase 3 in dependency order. Treat all MUST requirements, normative
> tables, algorithms, lifecycle rules, module boundaries, safety behavior, and
> acceptance criteria as authoritative. Preserve upstream-recognizable integration
> surfaces and supplied vendor code. Keep reusable behavior in `3drdNextFTC`, keep
> `3drdQuanomous` independent of NextFTC, and keep Decode/Osiris facts in TeamCode.
> Add meaningful tests with each behavior and maintain 100% line and branch
> coverage. Validate the reusable phase-2 endpoint independently before adding
> phase 3. Then validate the final project with the mandated Gradle command and
> `mkdocs build --strict`. Organize commits by the documented phases and conceptual
> dependencies. Do not inspect or cherry-pick implementation commits after the
> foundation. Do not silently invent missing robot behavior: record every required
> assumption in `REBUILD_AMBIGUITIES.md`, choose the safest minimal interpretation,
> and continue so the audit remains one-shot.

The instruction intentionally tells the agent to continue rather than pause for
questions. The ambiguity report is the documentation test output.

## Required phase-2 audit

Before phase 3, the auditor MUST record the phase-2 commit and prove:

- Kotlin/NextFTC/Next Control/Panels/Pedro dependencies resolve;
- `3drdQuanomous` has no NextFTC dependency;
- reusable modules contain no Decode types, Osiris names, or tuned robot values;
- neutral TeamCode constructs its OpModes and components;
- subsystem discovery, bindings lifecycle, config/persistence, commands, hardware,
  telemetry/logging, units, Drive/Nav foundations, and Quanomous behavior pass;
- all three owned modules meet line and branch thresholds; and
- the neutral APK assembles.

Only then should the auditor tag or record that commit and proceed to Decode.

## Required final audit

The final report MUST contain:

1. reconstructed commit list grouped into phases;
2. exact reusable endpoint commit;
3. full Gradle verification output summary;
4. strict documentation build result;
5. requirement IDs not demonstrably covered;
6. every ambiguity and chosen interpretation;
7. automated differences from documented acceptance behavior; and
8. physical checks that remain unverified without Osiris.

Physical checks are never inferred from unit tests. Mark them pending until the
hardware worksheet is executed on the robot.

## Turn audit findings into documentation

Classify each ambiguity:

- **Missing requirement:** add or refine a stable requirement and acceptance case.
- **Missing exact value:** add it to the relevant normative specification table.
- **Unexplained design choice:** update the architecture/decision record.
- **Missing implementation order:** update the appropriate phase rebuild page.
- **Missing executable example:** add a test or guide example.
- **Stale disagreement:** reconcile docs, tests, and current intended behavior;
  never simply describe both as correct.

Repeat the audit until a competent agent produces no material behavioral
assumptions. That, rather than page count, is the standard for one-shot completeness.
