package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.Servo.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ServoExTests : SubsystemTests() {
    @Before
    fun configureTelemetry() {
        TeamTelemetry.LEVEL = VERBOSE
        TeamTelemetry.FILTER = ""
    }

    @Test
    fun initializationResolvesTheConfiguredServo() {
        val servo = ServoEx("unconfigured")

        servo.initialize()

        assertNotNull(servo.servo)
    }

    @Test
    fun telemetryUsesHardwareNameAndStandardFields() {
        val servo = ServoEx("testServo")
        `when`(servo.servo.direction).thenReturn(REVERSE)
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        servo.update { position = 0.426 }

        verify(telemetry).addData("D | Test Servo | Position", "0.43" as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", true as Any)
    }

    @Test
    fun telemetryReportsForwardServos() {
        val servo = ServoEx("testServo")
        `when`(servo.servo.direction).thenReturn(FORWARD)
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        servo.tel()

        verify(telemetry).addData("V | Test Servo | Reversed", false as Any)
        assertEquals("", "".humanize())
        assertEquals("Test Servo", "testServo".humanize())
    }

    @Test
    fun hardwareTelemetryIsHiddenAtTheDefaultInfoLevel() {
        val servo = ServoEx("testServo")
        val telemetry = ActiveOpMode.telemetry
        TeamTelemetry.LEVEL = INFO
        clearInvocations(telemetry)

        servo.tel()

        verify(telemetry, never()).addData(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any())
    }
}
