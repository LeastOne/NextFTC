# Hardware and controls

This page records the current Decode/Osiris contract. Verify it against the Control
Hub configuration and physical robot before relying on it.

## Hardware map names

| Subsystem | Name | Type/purpose |
|---|---|---|
| Drive | `driveFrontLeft` | Mecanum front-left motor |
| Drive | `driveFrontRight` | Mecanum front-right motor |
| Drive | `driveBackLeft` | Mecanum rear-left motor |
| Drive | `driveBackRight` | Mecanum rear-right motor |
| Drive/localization | Pedro Pinpoint default device | GoBilda Pinpoint localizer; confirm configured name with Pedro version |
| Deflector | `deflector` | Positional servo |
| Gate | `gate` | Positional servo |
| Intake | `intake` | Roller motor |
| Intake | `bumperLeft` | Left bumper servo |
| Intake | `bumperRight` | Right bumper servo |
| Intake | `laser2` | Digital artifact sensor |
| Conveyor | `conveyor` | Conveyor motor |
| Flywheel | `flywheelLeft` | Left launcher motor |
| Flywheel | `flywheelRight` | Right launcher motor |
| Kickstand | `kickstandLeft` | Left kickstand servo |
| Kickstand | `kickstandRight` | Right kickstand servo |
| Vision | `limelight` | Limelight 3A |
| Vision | `turret` | Camera turret servo |
| Lights | `prism` | GoBilda Prism LED controller |

!!! warning "Pinpoint name"
    `PinpointConstants` currently relies on the Pedro library's default hardware
    name rather than setting one in TeamCode. Confirm that default when upgrading
    Pedro or creating the Control Hub configuration.

## Teleop controls

### Driver — gamepad 1

| Input | Action |
|---|---|
| Left stick Y | Forward/backward |
| Left stick X | Strafe left/right |
| Right stick X | Turn |
| D-pad down | Low drive power |
| D-pad left or right | Medium drive power |
| D-pad up | High drive power |
| Back + Start | Toggle robot-/field-centric mode |
| Left bumper + right bumper | Toggle kickstand |

Drive D-pad speed bindings are disabled while Back is held so the same D-pad can
edit configuration.

### Operator — gamepad 2

The operator chooses a mechanism with a face/control button and then uses D-pad:

| Chord/input | Up | Down | Left/right |
|---|---|---|---|
| Y + D-pad (Start not held) | Deflector up | Deflector down | — |
| Back + D-pad (Start not held) | Gate open | Gate close | — |
| A + D-pad (Start not held) | Intake forward | Intake reverse | Intake stop |
| X + D-pad (Start not held) | Conveyor forward | Conveyor reverse | Conveyor stop |
| B + D-pad (Start not held) | Flywheel forward | Flywheel reverse | Flywheel stop |
| Either trigger > 0.5 | Flywheel forward while held | — | Stop on release |
| Left bumper + right bumper | Toggle kickstand | — | — |

### Configuration — either gamepad during init or run

Hold Back to edit. D-pad up/down selects a setting and left/right changes its value.
A setting without `live = true` cannot change after Start. Auto installs these menu
bindings during init but does not install mechanism/drive controls.

## Safe-stop behavior

Drive breaks following. Intake, Conveyor, and Flywheel set their target state to
stop and directly write zero motor power. Vision stops Limelight. Servo mechanisms
that should hold their physical position remain at their last command.
