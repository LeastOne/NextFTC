package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.TelemetryLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CRServoExTests : SubsystemTests() {
    @Before
    fun configureTelemetry() {
        TeamTelemetry.LEVEL = VERBOSE
        TeamTelemetry.FILTER = ""
    }

    @Test
    fun initializationResolvesAndConfiguresTheServo() {
        var configured = false
        val servo = CRServoEx("configured") { configured = true }

        servo.initialize()

        assertNotNull(servo.servo)
        assertTrue(configured)
    }

    @Test
    fun telemetryReportsPowerAndDirection() {
        val servo = CRServoEx("testServo")
        servo.initialize()
        val telemetry = ActiveOpMode.telemetry
        clearInvocations(telemetry)

        `when`(servo.servo.direction).thenReturn(REVERSE)
        servo.update { power = 0.256 }
        `when`(servo.servo.direction).thenReturn(FORWARD)
        servo.update { }

        verify(telemetry, org.mockito.Mockito.times(2))
            .addData("D | Test Servo | Power", "0.26" as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", true as Any)
        verify(telemetry).addData("V | Test Servo | Reversed", false as Any)
    }
}
