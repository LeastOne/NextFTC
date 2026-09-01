# Traceability

This matrix maps requirement groups to the conceptual commit that introduced the
behavior, its current implementation area, and its primary executable evidence.
Individual test methods provide finer-grained behavior examples.

| Requirements | Commit | Current implementation | Primary tests |
|---|---|---|---|
| `REQ-FND-001` | `26cd1fdd` | Upstream repository history | Full Android build |
| `REQ-FND-002` | `091539e7` | `settings.gradle`, `gradle/gradle-daemon-jvm.properties` | Gradle sync/build |
| `REQ-FND-003` | `ccd80e9f` | `build.gradle`, `gradle.properties`, `lint.xml` | Lint and assemble output |
| `REQ-FND-004` | `1cea9159` | Root `*.cmd` scripts | Robot-side manual validation |
| `REQ-FND-005` | `e4d71312` | TeamCode source tree | Repository inspection |
| `REQ-PLT-001`–`005` | `69db65e6` | Module Gradle files and dependency catalog | All module builds |
| `REQ-PLT-023`–`026` | `276be2ef` | `nextftc.logging`, `nextftc.telemetry`, `Diagnostics` | `LoggingTests`, `TelemetryTests`, component and diagnostics tests |
| `REQ-PLT-030`–`032` | `d498d2e0` | `nextftc.hardware` | One wrapper test file per hardware type |
| `REQ-PLT-010`–`017` | `c0b42867` | `bindings`, `commands`, `subsystems` | Binding, command, discovery, adapter, and component tests |
| `REQ-PLT-020`–`022` | `8b70be56` | `config`, `ConfigSubsystem`, `Persistence`, `Storage` | Settings, config component/subsystem, persistence, and storage tests |
| `REQ-PLT-033`–`037` | `19604a24` | `DriveSubsystem`, `NavSubsystem`, `pedropathing` helpers | Drive/Nav and Pedro helper test suites |
| `REQ-PLT-042` | `4b3855f1` | `util.Debounce` | `DebounceTests` |
| `REQ-PLT-040`–`041` | `d7441bc7` | `3drdQuanomous` | Quanomous parse, compile, and storage tests |
| `REQ-SCF-001`–`006` | `29075d09` | Neutral `TeamCode` at the base tag | TeamCode OpMode, config, timing, constants, and telemetry tests |
| `REQ-SCF-007`, `REQ-QLT-001`–`007` | `3d35384f`, completed at `reusable-season-base` | `AGENTS.md`, documentation, inspection profiles, coverage rules | Full mandated verification and strict docs build |
| `REQ-QLT-008` | documentation layer | [First-robot walkthrough](../guides/first-robot.md), exact Osiris specifications, and [clean-room protocol](../rebuild/one-shot.md) | Strict docs build plus a recorded clean-room audit |
| `REQ-DEC-001` | `6b7e0567` | TeamCode Pedro `Constants`; [system specification](osiris-system.md#robot-and-pedro-constants) | `ConstantsTests` |
| `REQ-DEC-002` | `6b7e0567` | `Alliance`, `Side`, `Nav`; [navigation specification](osiris-navigation-vision.md#coordinate-contract) | `AllianceTests`, `SideTests`, `NavTests` |
| `REQ-DEC-003` | `6b7e0567` | `Config.onChange`, `resetStartingPose`; [system specification](osiris-system.md#driver-configuration) | `ConfigTests.changingAllianceOrSideResetsTheStartingPose` |
| `REQ-DEC-004` | `6b7e0567` | `Config.initialize/start/missingAutoSettings` | Config Auto initialization/readiness/start tests |
| `REQ-DEC-005` | `6b7e0567` | `Config.periodic`, `Telemetry.configWarning` | `ConfigTests.autonomousWarningAppearsAboveConfigUntilRequiredSettingsAreSelected` |
| `REQ-DEC-010` | `6b7e0567` | `Drive.driverControlled`; [driver motion specification](osiris-system.md#driver-controlled-motion) | `DriveTests.driverInputsUsePedroSignConvention` |
| `REQ-DEC-011` | `6b7e0567` | Drive scalar commands, centric supplier, controls | Drive power/toggle/default/retention tests |
| `REQ-DEC-012` | `6b7e0567` | Drive lock/correction functions and Next Control systems | Drive lock, correction, and status tests |
| `REQ-DEC-013` | `6b7e0567` | `Vision`; [vision specification](osiris-navigation-vision.md#vision-hardware-and-tuning) | `VisionTests` |
| `REQ-DEC-014` | `6b7e0567` | TeamCode `OpMode`, `PedroDrawingComponent`, Constants radius | `OpModeTests`, `ConstantsTests`; physical drawing check |
| `REQ-DEC-020` | `49f3fa67` | `Deflector`, `Gate`; [mechanism specification](osiris-mechanisms.md#deflector) | `DeflectorTests`, `GateTests` |
| `REQ-DEC-021` | `0dbce6ae` | `Intake`; [intake specification](osiris-mechanisms.md#intake) | `IntakeTests` |
| `REQ-DEC-022` | `0dbce6ae` | `Conveyor`; [conveyor specification](osiris-mechanisms.md#conveyor) | `ConveyorTests` |
| `REQ-DEC-023` | `0dbce6ae` | `Flywheel`; [flywheel specification](osiris-mechanisms.md#flywheel) | `FlywheelTests` |
| `REQ-DEC-024` | `b4f08db0` | `Kickstand`; [kickstand specification](osiris-mechanisms.md#kickstand) | `KickstandTests` |
| `REQ-DEC-025` | `b4f08db0` | `Lights`, unchanged GoBilda Prism vendor files | `LightsTests`; vendor-driver physical check |
| `REQ-DEC-026` | `29075d09`, refined in phase 3 | `Timing`; [timing specification](osiris-mechanisms.md#timing) | `TimingTests` |
| `REQ-DEC-030` | `297645cd` | `Auto.execute/remaining`, Auto OpMode; [autonomous specification](osiris-autonomous.md#entry-and-time-budget) | subsystem and OpMode `AutoTests` |
| `REQ-DEC-031` | `297645cd` | TeamCode `Quanomous`; [JSON vocabulary](osiris-autonomous.md#decode-json-vocabulary) | `QuanomousTests.compilesSeasonCommandVariants` |
| `REQ-DEC-032` | `297645cd` | Drive deferred route properties and Auto deferred deposit/time wrapper | Drive path/route tests and Auto composition tests |
| `REQ-DEC-033` | `297645cd` | `Auto.stopAll` plus Drive/Intake/Conveyor/Flywheel/Vision lifecycle stops | Auto composition and affected subsystem lifecycle-stop tests |

## Coverage boundary

`3drdNextFTC` coverage includes its own tests plus the consumer `TeamCode` tests
where generic library behavior is exercised through real subsystem usage.
`3drdQuanomous` remains independently covered. `TeamCode` covers all owned Kotlin
production behavior; third-party GoBilda Prism Java is vendor code and is not
treated as 3DRD-authored logic.

## Keeping this current

When behavior changes:

1. update or add the stable requirement;
2. update the relevant architecture rationale;
3. update the reconstruction step if a clean implementation would change;
4. add or revise the associated test;
5. update this matrix if ownership or the conceptual commit boundary changes.
