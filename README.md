# 3DRD NextFTC

This repository is 3D Robotics Duluth's requirements-driven FTC robot platform and
its Decode-season implementation for the Osiris robot. It starts from the official
FIRST Tech Challenge Robot Controller v11.2 repository, adds a reusable Kotlin and
NextFTC platform, and then layers the robot- and season-specific code on top.

## Documentation

The canonical documentation lives in [`docs/`](docs/index.md). Begin with the
[guides](docs/guides/new-season.md), then use the deeper material as needed:

- [start a new season and build the first robot](docs/guides/new-season.md);
- [architecture and design rationale](docs/architecture/overview.md);
- [requirements and traceability](docs/requirements/index.md);
- [a complete reconstruction guide](docs/rebuild/index.md);
- [reference tables for dependencies, settings, hardware, and controls](docs/reference/modules-dependencies.md).

The same Markdown is published as a searchable, navigable GitHub Pages site by the
repository's documentation workflow. GitHub Pages must be configured to use
**GitHub Actions** as its source before the first deployment.

For local preview:

```powershell
python -m venv .venv-docs
.\.venv-docs\Scripts\python -m pip install -r requirements-docs.txt
.\.venv-docs\Scripts\mkdocs serve
```

## Start a Season

Start a new robot season from
[3DRD NextFTC Quickstart](https://github.com/3DRoboticsDuluth/NextFTC-Quickstart),
not from this Decode/Osiris tip. Retain Quickstart as the `quickstart` remote so the
season preserves the complete FTC/platform history and can rebase onto future
corrections. See [Start a new season](docs/guides/new-season.md) for the exact
workflow, collaboration cautions, and replacement checklist.

## Verification

All reusable modules and TeamCode enforce 100% line and branch coverage. Run the
repository verification command before merging:

```powershell
.\gradlew :3drdNextFTC:check :3drdNextFTC:unitTestCoverage :3drdQuanomous:check :3drdQuanomous:unitTestCoverage :TeamCode:check :TeamCode:unitTestCoverage :TeamCode:assembleDebug
```

The upstream FTC release notes remain available in the Git history at the
[FTC foundation commit](../../tree/26cd1fdd).
