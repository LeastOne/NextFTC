package org.firstinspires.ftc.threedrd.nextftc.hardware

import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ServoExTests : SubsystemTests() {
    @Test
    fun initializationResolvesTheConfiguredServo() {
        val servo = ServoEx("unconfigured")

        servo.initialize()

        assertNotNull(servo.servo)
    }

    @Test
    fun updateAppliesTheActionAndReportsTheServo() {
        val servo = ServoEx("test")
        var actionApplied = false
        var reported: ServoEx? = null
        HardwareTelemetry.servo(servo)
        HardwareTelemetry.servo = { reported = this }

        servo.update { actionApplied = true }

        assertTrue(actionApplied)
        assertSame(servo, reported)
    }
}
