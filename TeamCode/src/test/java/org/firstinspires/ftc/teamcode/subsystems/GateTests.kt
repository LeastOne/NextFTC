package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.Servo
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import com.qualcomm.robotcore.util.RobotLog
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.teamcode.subsystems.Gate.CLOSE
import org.firstinspires.ftc.teamcode.subsystems.Gate.HOLD
import org.firstinspires.ftc.teamcode.subsystems.Gate.MAX
import org.firstinspires.ftc.teamcode.subsystems.Gate.MIN
import org.firstinspires.ftc.teamcode.subsystems.Gate.OPEN
import org.firstinspires.ftc.teamcode.subsystems.Gate.POS
import org.firstinspires.ftc.teamcode.subsystems.Gate.close
import org.firstinspires.ftc.teamcode.subsystems.Gate.hold
import org.firstinspires.ftc.teamcode.subsystems.Gate.open
import org.firstinspires.ftc.teamcode.subsystems.Gate.periodic
import org.firstinspires.ftc.teamcode.subsystems.Gate.servo
import org.firstinspires.ftc.teamcode.subsystems.Gate.controls
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mockStatic
import java.lang.reflect.Modifier

class GateTests : SubsystemTests() {
    @Before
    fun resetConfig() {
        MIN = 0.0
        MAX = 0.5
        CLOSE = 0.0
        HOLD = 0.5
        OPEN = 1.0
        POS = CLOSE
    }

    @Test
    fun servoIsConfigured() {
        verify(servo.servo).scaleRange(0.0, 0.5)
        verify(servo.servo).direction = Servo.Direction.REVERSE
    }

    @Test
    fun settingsArePanelsConfigurable() {
        assertTrue(Gate::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(0.0, MIN, 0.0)
        assertEquals(0.5, MAX, 0.0)
        assertEquals(0.5, HOLD, 0.0)
        assertEquals(1.0, OPEN, 0.0)
        assertEquals(
            setOf("MIN", "MAX", "CLOSE", "HOLD", "OPEN", "POS"),
            Gate::class.java.declaredFields
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
        POS = 0.74

        periodic()

        verify(servo.servo).position = 0.74
    }

    @Test
    fun commandsUseCurrentConfiguredPositions() {
        OPEN = 0.9
        HOLD = 0.4
        CLOSE = 0.1

        open.start()
        assertEquals(0.9, POS, 0.0)

        hold.start()
        assertEquals(0.4, POS, 0.0)

        close.start()
        assertEquals(0.1, POS, 0.0)
    }

    @Test
    fun commandsAreNamedAfterTheirProperties() {
        assertEquals("Gate.open", open.name)
        assertEquals("Gate.hold", hold.name)
        assertEquals("Gate.close", close.name)
        assertTrue(open.requirements.contains(Gate))
    }

    @Test
    fun commandsLogTheirPropertyNames() {
        val telemetryLog = ActiveOpMode.telemetry.log()
        clearInvocations(telemetryLog)
        val logLevel = Logging.LEVEL
        val logFilter = Logging.FILTER

        try {
            Logging.LEVEL = DEBUG
            Logging.FILTER = ""
            mockStatic(RobotLog::class.java).use { open.start() }

            verify(telemetryLog).add("D | Commands | Executed | Gate.open")
        } finally {
            Logging.LEVEL = logLevel
            Logging.FILTER = logFilter
        }
    }
}
