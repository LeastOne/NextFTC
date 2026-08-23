package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.Assert.assertEquals

class MotorExTests : SubsystemTests() {
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
    fun getsAndSetsVelocityAsAPercentageOfTheConfiguredMaximum() {
        val motor = MotorEx("percentage")
        motor.initialize()
        val type = mock(MotorConfigurationType::class.java)
        `when`(motor.motor.motorType).thenReturn(type)
        `when`(type.achieveableMaxTicksPerSecond).thenReturn(500.0)
        `when`(motor.motor.velocity).thenReturn(125.0)

        assertEquals(0.25, motor.velocityPercentage, 0.0)
        motor.velocityPercentage = 0.5

        verify(motor.motor).velocity = 250.0
    }

    @Test
    fun updateAppliesTheActionAndReportsTheMotor() {
        val motor = MotorEx("test")
        var actionApplied = false
        var reported: MotorEx? = null
        HardwareTelemetry.motor(motor)
        HardwareTelemetry.motor = { reported = this }

        motor.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(motor, reported)
    }
}
