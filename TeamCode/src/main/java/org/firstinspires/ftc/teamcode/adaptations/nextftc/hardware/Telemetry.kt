package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE as CR_REVERSE
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import org.firstinspires.ftc.threedrd.nextftc.hardware.CRServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.IMUEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.HardwareTelemetry
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.ServoEx
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry.add
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.VERBOSE

fun configureHardwareTelemetry() {
    HardwareTelemetry.servo = { tel() }
    HardwareTelemetry.continuousServo = { tel() }
    HardwareTelemetry.motor = { tel() }
    HardwareTelemetry.imu = { tel() }
}

fun ServoEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Position", "%.2f".format(position))
    add(source, VERBOSE, "Reversed", servo.direction == REVERSE)
}

fun CRServoEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Power", "%.2f".format(power))
    add(source, VERBOSE, "Reversed", servo.direction == CR_REVERSE)
}

fun MotorEx.tel() {
    val source = name.humanize()
    add(source, VERBOSE, "Current (A)", "%.1f".format(motor.getCurrent(AMPS)))
    add(source, DEBUG, "Power", "%.2f".format(power))
    add(source, DEBUG, "Velocity", "%.1f".format(velocity))
    add(source, DEBUG, "Position", motor.currentPosition)
    add(source, VERBOSE, "Velocity (%)", "%.1f".format(velocity / motor.motorType.achieveableMaxTicksPerSecond * 100))
    add(source, VERBOSE, "RPM", "%.0f".format(velocity / motor.motorType.ticksPerRev * 60))
}

fun IMUEx.tel() {
    val source = name.humanize()
    add(source, DEBUG, "Yaw (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getYaw(DEGREES)))
    add(source, DEBUG, "Pitch (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getPitch(DEGREES)))
    add(source, DEBUG, "Roll (deg)", "%.1f".format(imu.robotYawPitchRollAngles.getRoll(DEGREES)))
}

fun String.humanize() = replace(Regex("([a-z])([A-Z])"), "$1 $2")
    .replaceFirstChar { it.uppercase() }
