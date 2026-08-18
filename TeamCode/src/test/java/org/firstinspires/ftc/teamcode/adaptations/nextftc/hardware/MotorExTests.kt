package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class MotorExTests : SubsystemTests() {
    @Before
    fun configureTelemetry() {
        TeamTelemetry.LEVEL = VERBOSE
        TeamTelemetry.FILTER = ""
    }

    @Test
    fun initializationResolvesAndConfiguresTheMotor() {
        var configured = false
        val motor = MotorEx("configured") { configured = true }

        motor.initialize()

        assertNotNull(motor.motor)
        assertNotNull(motor.motorEx)
        assertTrue(configured)
    }

    @Test
    fun fluentConfigurationReturnsTheAdapter() {
        val motor = MotorEx("fluent")
        motor.initialize()

        assertSame(
            motor,
            motor.reversed().zeroed().atPosition(12.0).floatMode().brakeMode()
        )
    }

    @Test
    fun telemetryUsesTheEstablishedMotorFields() {
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
}
