package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD as CR_FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE as CR_REVERSE
import com.qualcomm.robotcore.hardware.Servo.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.firstinspires.ftc.threedrd.nextftc.hardware.CRServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.IMUEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.ServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.VERBOSE
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TelemetryTests : SubsystemTests() {
    @Before
    fun configureTelemetry() {
        configureHardwareTelemetry()
        TeamTelemetry.LEVEL = VERBOSE
        TeamTelemetry.FILTER = ""
    }

    @Test
    fun servoTelemetryUsesHardwareNameAndStandardFields() {
        val servo = ServoEx("testServo")
        `when`(servo.servo.direction).thenReturn(REVERSE)
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        servo.update { position = 0.426 }
        `when`(servo.servo.direction).thenReturn(FORWARD)
        servo.tel()

        verify(telemetry, times(2)).addData("D | Test Servo | Position", "0.43" as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", true as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", false as Any)
        assertEquals("", "".humanize())
        assertEquals("Test Servo", "testServo".humanize())
    }

    @Test
    fun continuousServoTelemetryReportsPowerAndDirection() {
        val servo = CRServoEx("testServo")
        servo.initialize()
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        `when`(servo.servo.direction).thenReturn(CR_REVERSE)
        servo.update { power = 0.256 }
        `when`(servo.servo.direction).thenReturn(CR_FORWARD)
        servo.update { }

        verify(telemetry, times(2)).addData("D | Test Servo | Power", "0.26" as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", true as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", false as Any)
    }

    @Test
    fun motorTelemetryUsesTheEstablishedFields() {
        val motor = MotorEx("testMotor")
        motor.initialize()
        val type = mock(MotorConfigurationType::class.java)
        `when`(motor.motor.getCurrent(AMPS)).thenReturn(1.234)
        `when`(motor.motor.velocity).thenReturn(241.23)
        `when`(motor.motor.currentPosition).thenReturn(42)
        `when`(motor.motor.motorType).thenReturn(type)
        `when`(type.achieveableMaxTicksPerSecond).thenReturn(480.0)
        `when`(type.ticksPerRev).thenReturn(120.0)
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        motor.update { power = 0.456 }

        verify(telemetry).addData("V | Test Motor | Current (A)", "1.2" as Any)
        verify(telemetry).addData("D | Test Motor | Power", "0.46" as Any)
        verify(telemetry).addData("D | Test Motor | Velocity", "241.2" as Any)
        verify(telemetry).addData("D | Test Motor | Position", 42 as Any)
        verify(telemetry).addData("V | Test Motor | Velocity (%)", "50.3" as Any)
        verify(telemetry).addData("V | Test Motor | RPM", "121" as Any)
    }

    @Test
    fun imuTelemetryReportsOrientation() {
        val imu = IMUEx("testImu")
        imu.initialize()
        val angles = mock(YawPitchRollAngles::class.java)
        `when`(imu.imu.robotYawPitchRollAngles).thenReturn(angles)
        `when`(angles.getYaw(DEGREES)).thenReturn(10.06)
        `when`(angles.getPitch(DEGREES)).thenReturn(20.04)
        `when`(angles.getRoll(DEGREES)).thenReturn(30.06)
        val telemetry = ActiveOpMode.telemetry
        var updated = false
        clearInvocations(telemetry)

        imu.update { updated = true }

        assertTrue(updated)
        verify(telemetry).addData("D | Test Imu | Yaw (deg)", "10.1" as Any)
        verify(telemetry).addData("D | Test Imu | Pitch (deg)", "20.0" as Any)
        verify(telemetry).addData("D | Test Imu | Roll (deg)", "30.1" as Any)
    }

    @Test
    fun hardwareTelemetryIsHiddenAtTheDefaultInfoLevel() {
        val servo = ServoEx("testServo")
        val telemetry = ActiveOpMode.telemetry
        TeamTelemetry.LEVEL = INFO
        clearInvocations(telemetry)

        servo.tel()

        verify(telemetry, never()).addData(anyString(), any())
    }
}
