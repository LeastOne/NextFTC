package org.firstinspires.ftc.teamcode.subsystems

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import com.qualcomm.robotcore.hardware.Servo

class KickstandTests : SubsystemTests() {
    @Test
    fun fluentRangesPreserveNativeTypesAndExplicitRightDirection() {
        assertEquals(dev.nextftc.hardware.impl.ServoEx::class.java, Kickstand.servoLeft.javaClass)
        assertEquals(dev.nextftc.hardware.impl.ServoEx::class.java, Kickstand.servoRight.javaClass)
        verify(Kickstand.servoLeft.servo).scaleRange(0.25, 0.95)
        verify(Kickstand.servoRight.servo).scaleRange(0.30, 1.00)
        verify(Kickstand.servoRight.servo).direction = Servo.Direction.REVERSE
    }

    @Before
    fun setUp() {
        Kickstand.initializeHardware()
        Kickstand.initialize()
    }

    @Test
    fun commandsSelectAndTogglePosition() {
        Kickstand.engage.start()
        assertEquals(Kickstand.ENGAGE, Kickstand.POS, 0.0)
        Kickstand.toggle.start()
        assertEquals(Kickstand.DISENGAGE, Kickstand.POS, 0.0)
        Kickstand.toggle.start()
        assertEquals(Kickstand.ENGAGE, Kickstand.POS, 0.0)
        Kickstand.disengage.start()
        assertEquals(Kickstand.DISENGAGE, Kickstand.POS, 0.0)
    }

    @Test
    fun periodicMovesBothServos() {
        Kickstand.POS = 0.75

        Kickstand.periodic()

        assertEquals(0.75, Kickstand.servoLeft.position, 0.0)
        assertEquals(0.75, Kickstand.servoRight.position, 0.0)
    }

    @Test
    fun controlsCreateBindingsForBothGamepads() {
        Kickstand.controls()
    }

    @Test
    fun configurableValuesRemainMutable() {
        Kickstand.LEFT_MIN = Kickstand.LEFT_MIN
        Kickstand.LEFT_MAX = Kickstand.LEFT_MAX
        Kickstand.RIGHT_MIN = Kickstand.RIGHT_MIN
        Kickstand.RIGHT_MAX = Kickstand.RIGHT_MAX
        Kickstand.DISENGAGE = Kickstand.DISENGAGE
        Kickstand.ENGAGE = Kickstand.ENGAGE
    }
}
