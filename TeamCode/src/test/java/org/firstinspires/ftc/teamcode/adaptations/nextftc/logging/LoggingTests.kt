package org.firstinspires.ftc.teamcode.adaptations.nextftc.logging

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.RobotLog
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.LambdaCommand
import dev.nextftc.core.subsystems.Subsystem
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.debug
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.info
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.warn
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LoggingTests {
    private object TestSubsystem : Subsystem

    private lateinit var telemetry: Telemetry
    private lateinit var telemetryLog: Telemetry.Log

    @Before
    fun setUp() {
        CommandManager.cancelAll()
        CommandManager.run()

        telemetry = mock(Telemetry::class.java)
        telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply {
            this.telemetry = this@LoggingTests.telemetry
        }

        Logging.LOG_LEVEL = LogLevel.INFO
        Logging.TELEMETRY_LEVEL = LogLevel.DEBUG
        Logging.TELEMETRY_TAG = ""
        Logging.TELEMETRY_TEXT = ""
        Logging.preInit()
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()

        Logging.LOG_LEVEL = LogLevel.DEBUG
        Logging.TELEMETRY_LEVEL = LogLevel.VERBOSE
        Logging.TELEMETRY_TAG = ""
        Logging.TELEMETRY_TEXT = ""
    }

    @Test
    fun directValuesAreSentImmediatelyToAcceptedDestinations() {
        TestSubsystem.debug("Position", "0.42")
        TestSubsystem.info("Ready")

        verify(telemetry).addData("TestSubsystem (Position)", "0.42" as Any)
        verify(telemetryLog).add("INFO TestSubsystem | Ready")
    }

    @Test
    fun lazyValuesAreNotEvaluatedWhenBothDestinationsRejectThem() {
        var evaluations = 0
        Logging.LOG_LEVEL = LogLevel.OFF
        Logging.TELEMETRY_LEVEL = LogLevel.INFO

        TestSubsystem.debug("Position") { ++evaluations }
        assertEquals(0, evaluations)

        Logging.TELEMETRY_LEVEL = LogLevel.DEBUG
        TestSubsystem.debug("Position") { ++evaluations }
        assertEquals(1, evaluations)

        Logging.TELEMETRY_TAG = "Gate"
        TestSubsystem.debug("Position") { ++evaluations }
        assertEquals(1, evaluations)
    }

    @Test
    fun nullValuesAreFormattedForBothTelemetryForms() {
        Logging.LOG_LEVEL = LogLevel.OFF
        Logging.TELEMETRY_LEVEL = LogLevel.INFO

        TestSubsystem.info("Optional", null)
        Logging.log(TestSubsystem, LogLevel.INFO, null, null)

        verify(telemetry).addData("TestSubsystem (Optional)", null as Any?)
        verify(telemetryLog).add("INFO TestSubsystem | null")
    }

    @Test
    fun telemetryFiltersByLevelTagAndText() {
        Logging.TELEMETRY_LEVEL = LogLevel.DEBUG
        Logging.TELEMETRY_TAG = "test"
        Logging.TELEMETRY_TEXT = "position 0.42"

        TestSubsystem.debug("Position", "0.42")
        TestSubsystem.info("Mode", "Automatic")
        TestSubsystem.info("Position reached")

        verify(telemetry).addData("TestSubsystem (Position)", "0.42" as Any)
        verify(telemetry, never()).addData("TestSubsystem (Mode)", "Automatic" as Any)
        verify(telemetryLog, never()).add("INFO TestSubsystem | Position reached")
    }

    @Test
    fun telemetryRejectsNonmatchingTags() {
        Logging.LOG_LEVEL = LogLevel.OFF
        Logging.TELEMETRY_TAG = "Gate"

        TestSubsystem.info("Ready")

        verify(telemetryLog, never()).add("INFO TestSubsystem | Ready")
    }

    @Test
    fun robotLogLevelAcceptsLazyMessagesIndependentlyOfTelemetry() {
        var evaluations = 0
        Logging.LOG_LEVEL = LogLevel.WARN
        Logging.TELEMETRY_LEVEL = LogLevel.OFF

        TestSubsystem.info { evaluations++; "Ready" }
        TestSubsystem.warn { evaluations++; "Blocked" }

        assertEquals(1, evaluations)
        verify(telemetryLog, never()).add("WARN TestSubsystem | Blocked")
    }

    @Test
    fun componentUpdatesTelemetryDuringWaitAndActiveLoops() {
        Logging.preWaitForStart()
        Logging.postWaitForStart()
        Logging.postUpdate()

        verify(telemetry, times(2)).update()
    }

    @Test
    fun levelsAcceptOnlyEnabledSeverities() {
        LogLevel.entries.forEach { threshold ->
            LogLevel.entries.forEach { level ->
                val expected = threshold != LogLevel.OFF &&
                    level != LogLevel.OFF && level.ordinal >= threshold.ordinal
                assertEquals(expected, with(Logging) { threshold.accepts(level) })
            }
        }
    }

    @Test
    fun textMatchingIsCaseInsensitive() {
        assertTrue(with(Logging) { "Deflector Position".matches("FLECT") })
        assertTrue(with(Logging) { "Anything".matches("") })
        assertFalse(with(Logging) { "Deflector".matches("Gate") })
    }

    @Test
    fun everySeverityMapsToRobotLog() {
        mockStatic(RobotLog::class.java).use { robotLog ->
            Logging.writeToRobotLog(LogLevel.VERBOSE, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.DEBUG, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.INFO, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.WARN, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.ERROR, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.ASSERT, "Tag", "Message")
            Logging.writeToRobotLog(LogLevel.OFF, "Tag", "Message")

            robotLog.verify { RobotLog.vv("Tag", "Message") }
            robotLog.verify { RobotLog.dd("Tag", "Message") }
            robotLog.verify { RobotLog.ii("Tag", "Message") }
            robotLog.verify { RobotLog.ww("Tag", "Message") }
            robotLog.verify { RobotLog.ee("Tag", "Message") }
            robotLog.verify { RobotLog.aa("Tag", "Message") }
        }
    }

    @Test
    fun commandSnapshotsAreLoggedOnlyWhenTheyChange() {
        val drive = LambdaCommand("Drive").setIsDone { false }
        CommandManager.scheduleCommand(drive)
        CommandManager.run()

        Logging.preUpdate()
        Logging.preUpdate()

        verify(telemetryLog).add("DEBUG Commands | Running | Drive")

        CommandManager.cancelCommand(drive)
        CommandManager.run()
        Logging.preUpdate()

        verify(telemetryLog).add("DEBUG Commands | Idle")
    }

    @Test
    fun commandSnapshotsExcludeNullCommands() {
        val nullCommand = LambdaCommand("NullCommand").setIsDone { false }
        CommandManager.scheduleCommand(nullCommand)
        CommandManager.run()

        Logging.preUpdate()

        verify(telemetryLog, never()).add("DEBUG Commands | Running | NullCommand")
    }
}
