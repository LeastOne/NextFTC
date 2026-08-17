package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.Hardware
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
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
        Logging.telemetry = telemetry
        Logging.LOG_LEVEL = OFF
        Logging.TELEMETRY_LEVEL = INFO
        Logging.TELEMETRY_FILTER = ""
    }

    @After
    fun tearDown() {
        Logging.LOG_LEVEL = DEBUG
        Logging.TELEMETRY_LEVEL = INFO
        Logging.TELEMETRY_FILTER = ""
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
            "HardwareSubsystem (Status)", "Disabled (see Logcat)"
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

        subsystem.initializeHardware()
        subsystem.reportDisabled()

        assertTrue(subsystem.disabled)
        assertEquals(1, hardware.initializations)
        verify(telemetry).addData(
            "HardwareSubsystem (Hardware)",
            "bad: java.lang.IllegalStateException: missing" as Any
        )
        verify(telemetry).addData(
            "HardwareSubsystem (Status)", "Disabled (see Logcat)" as Any
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
