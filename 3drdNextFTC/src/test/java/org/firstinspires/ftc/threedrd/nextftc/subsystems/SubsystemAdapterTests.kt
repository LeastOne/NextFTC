package org.firstinspires.ftc.threedrd.nextftc.subsystems

import org.firstinspires.ftc.threedrd.nextftc.hardware.Hardware
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.RobotLog
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class SubsystemAdapterTests {
    private object TestSubsystem : Subsystem()

    private class TestHardware(
        override val name: String,
        var failure: Exception? = null
    ) : Hardware {
        var initializations = 0

        override fun initialize() {
            initializations++
            failure?.let { throw it }
        }
    }

    private class HardwareSubsystem(
        val device: TestHardware,
        val duplicate: TestHardware? = null
    ) : Subsystem()

    private class CommandSubsystem(val device: TestHardware) : Subsystem() {
        var executions = 0
        val delegated by instant { executions++ }
    }

    private lateinit var telemetry: Telemetry

    @Before
    fun setUp() {
        telemetry = mock(Telemetry::class.java)
        val telemetryLog = mock(Telemetry.Log::class.java)
        org.mockito.Mockito.`when`(telemetry.log()).thenReturn(telemetryLog)
        org.mockito.Mockito.`when`(telemetryLog.capacity).thenReturn(9)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = this@SubsystemAdapterTests.telemetry }
        Logging.FILTER = ""
        Logging.DISPLAY_FILTER = ""
        TeamTelemetry.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        TeamTelemetry.initialize()
        Logging.LEVEL = OFF
        TeamTelemetry.LEVEL = INFO
    }

    @Test
    fun createsOwnedInstantCommands() {
        var executions = 0

        val unnamed = TestSubsystem.instant(Runnable { executions++ })
        val named = TestSubsystem.instant("Reset", Runnable { executions++ })

        assertTrue(unnamed.requirements.contains(TestSubsystem))
        assertTrue(named.requirements.contains(TestSubsystem))
        assertEquals("Reset", named.name)

        unnamed.start()
        named.start()
        TestSubsystem.start()
        assertEquals(2, executions)
    }

    @Test
    fun discoversEachHardwarePropertyOnlyOnce() {
        val hardware = TestHardware("ready")
        val subsystem = HardwareSubsystem(hardware, hardware)

        subsystem.initializeHardware()

        assertFalse(subsystem.disabled)
        assertTrue(subsystem.errors.isEmpty())
        assertEquals(1, hardware.initializations)
        verify(telemetry, never()).addData(
            "E | HardwareSubsystem | Status", "Disabled (see Logcat)"
        )
    }

    @Test
    fun retriesFailedHardwareForTheNextInitialization() {
        val hardware = TestHardware("recovering", IllegalStateException("missing"))
        val subsystem = HardwareSubsystem(hardware)
        subsystem.initializeHardware()

        hardware.failure = null
        subsystem.initializeHardware()

        assertFalse(subsystem.disabled)
        assertEquals(2, hardware.initializations)
    }

    @Test
    fun failedHardwareDisablesAndReportsItsSubsystem() {
        val hardware = TestHardware("bad", IllegalStateException("missing"))
        val subsystem = HardwareSubsystem(hardware)

        mockStatic(RobotLog::class.java).use { robotLog ->
            subsystem.initializeHardware()
            subsystem.reportDisabled()

            robotLog.verify {
                RobotLog.ee(
                    "HardwareSubsystem",
                    "Hardware | bad: java.lang.IllegalStateException: missing"
                )
            }
        }

        assertTrue(subsystem.disabled)
        assertEquals(1, hardware.initializations)
        verify(telemetry).addData(
            "E | HardwareSubsystem | Status", "Disabled (see Logcat)" as Any
        )
    }

    @Test
    fun disabledSubsystemCommandsDoNotExecute() {
        val subsystem = CommandSubsystem(
            TestHardware("bad", IllegalStateException("missing"))
        )
        val direct = subsystem.instant { subsystem.executions++ }

        subsystem.initializeHardware()
        direct.start()
        subsystem.delegated.start()

        assertEquals(0, subsystem.executions)
    }
}
