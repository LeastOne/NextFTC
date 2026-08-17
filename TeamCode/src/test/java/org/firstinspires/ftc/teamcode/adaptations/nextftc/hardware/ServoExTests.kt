package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.Servo.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ServoExTests : SubsystemTests() {
    @Before
    fun configureLogging() {
        Logging.LOG_LEVEL = OFF
        Logging.TELEMETRY_LEVEL = VERBOSE
        Logging.TELEMETRY_FILTER = ""
    }

    @After
    fun resetLogging() {
        Logging.LOG_LEVEL = DEBUG
        Logging.TELEMETRY_LEVEL = INFO
        Logging.TELEMETRY_FILTER = ""
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
        servo.position = 0.42
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        servo.telemetry()

        verify(telemetry).addData("testServo (pos)", "0.42" as Any)
        verify(telemetry).addData("testServo (rev)", true as Any)
    }

    @Test
    fun telemetryReportsForwardServos() {
        val servo = ServoEx("testServo")
        `when`(servo.servo.direction).thenReturn(FORWARD)
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        servo.telemetry()

        verify(telemetry).addData("testServo (rev)", false as Any)
    }
}
