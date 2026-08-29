package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.subsystems.Deflector.INC
import org.firstinspires.ftc.teamcode.subsystems.Deflector.MAX
import org.firstinspires.ftc.teamcode.subsystems.Deflector.MIN
import org.firstinspires.ftc.teamcode.subsystems.Deflector.POS
import org.firstinspires.ftc.teamcode.subsystems.Deflector.down
import org.firstinspires.ftc.teamcode.subsystems.Deflector.periodic
import org.firstinspires.ftc.teamcode.subsystems.Deflector.servo
import org.firstinspires.ftc.teamcode.subsystems.Deflector.controls
import org.firstinspires.ftc.teamcode.subsystems.Deflector.up
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.lang.reflect.Modifier

class DeflectorTests : SubsystemTests() {
    @Before
    fun resetConfiguration() {
        MIN = 0.0
        MAX = 0.8
        POS = 0.5
        INC = 0.02
    }

    @Test
    fun servoIsConfigured() {
        verify(servo.servo).scaleRange(0.0, 0.8)
        verify(servo.servo).direction = Servo.Direction.REVERSE
    }

    @Test
    fun settingsArePanelsConfigurable() {
        assertTrue(Deflector::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(0.0, MIN, 0.0)
        assertEquals(0.8, MAX, 0.0)
        assertEquals(0.02, INC, 0.0)
        assertEquals(
            setOf("MIN", "MAX", "POS", "INC"),
            Deflector::class.java.declaredFields
                .filter { !it.isSynthetic && Modifier.isStatic(it.modifiers) && !Modifier.isFinal(it.modifiers) }
                .map { it.name }
                .toSet()
        )
    }

    @Test
    fun controlsCanBeBoundForTeleop() {
        controls()
    }

    @Test
    fun periodicUpdatesServo() {
        POS = 0.73

        periodic()

        verify(servo.servo).position = 0.73
    }

    @Test
    fun movesInEitherDirection() {
        INC = 0.03

        up.start()
        assertEquals(0.53, POS, 0.0)

        down.start()
        assertEquals(0.5, POS, 0.0)
    }

    @Test
    fun movementIsClamped() {
        POS = 0.99
        up.start()
        assertEquals(1.0, POS, 0.0)

        POS = 0.01
        down.start()
        assertEquals(0.0, POS, 0.0)
    }

    @Test
    fun commandsAreNamedAfterTheirProperties() {
        assertEquals("Deflector.up", up.name)
        assertEquals("Deflector.down", down.name)
    }
}
