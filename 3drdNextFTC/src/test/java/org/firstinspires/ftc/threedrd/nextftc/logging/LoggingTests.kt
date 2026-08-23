package org.firstinspires.ftc.threedrd.nextftc.logging

import com.bylazar.configurables.annotations.Configurable
import com.bylazar.configurables.annotations.IgnoreConfigurable
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.RobotLog
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.ASSERT
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.ERROR
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.WARN
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LoggingTests {
    private lateinit var telemetryLog: Telemetry.Log

    @Before
    fun setUp() {
        val telemetry = mock(Telemetry::class.java)
        telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        `when`(telemetryLog.capacity).thenReturn(20)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = telemetry }
        Logging.FILTER = ""
        Logging.DISPLAY_FILTER = ""
        Logging.initialize()
        Logging.LEVEL = VERBOSE
    }

    @Test
    fun settingsArePanelsConfigurable() {
        assertTrue(Logging::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(setOf("LEVEL", "FILTER"), Logging::class.java.declaredFields
            .filter { !it.isSynthetic && !java.lang.reflect.Modifier.isFinal(it.modifiers) }
            .filterNot { it.isAnnotationPresent(IgnoreConfigurable::class.java) }
            .map { it.name }.toSet())
    }

    @Test
    fun diagnosticsAreSampledEachFrameUnlessPanelsOverridesTheLevel() {
        val diagnostics = TestDiagnostics()
        Logging.bind(Diagnostics(diagnostics))
        assertEquals(INFO, Logging.LEVEL)
        assertEquals("Gate", Logging.DISPLAY_FILTER)
        Logging.beginFrame()

        diagnostics.filter = "Deflector"
        Logging.beginFrame()
        assertEquals(INFO, Logging.LEVEL)
        assertEquals("Deflector", Logging.DISPLAY_FILTER)

        diagnostics.level = Diagnostics.Level.WARN
        Logging.beginFrame()
        assertEquals(WARN, Logging.LEVEL)

        Logging.LEVEL = ERROR
        diagnostics.level = Diagnostics.Level.INFO
        Logging.beginFrame()
        assertEquals(ERROR, Logging.LEVEL)

        Logging.bind(null)
        assertEquals(OFF, Logging.LEVEL)
        assertEquals("", Logging.DISPLAY_FILTER)
    }

    @Test
    fun commonFilterCanHideAnOtherwiseVisibleEntry() {
        Logging.DISPLAY_FILTER = "Deflector"
        Logging.FILTER = ""

        assertFalse(Logging.visible(LogEntry(INFO, "Gate", "Opened")))
    }

    @Test
    fun unchangedDisplaySettingsDoNotRebuildHistory() {
        Logging.initialize()

        Logging.beginFrame()

        verify(telemetryLog, org.mockito.Mockito.never()).clear()
    }

    private class TestDiagnostics(
        var level: Diagnostics.Level = Diagnostics.Level.INFO,
        var filter: String = "Gate"
    )

    @Test
    fun loggerConvenienceMethodsCreateLevelledEvents() {
        val logger = Logger("Test")
        assertEquals("Test", logger.tag)

        mockStatic(RobotLog::class.java).use {
            logger.verbose("Verbose")
            logger.debug("Debug")
            logger.info("Info")
            logger.warn("Warn")
            logger.error("Error")
            logger.fatal("Fatal")
            logger.add(INFO) { "Lazy" }
            logger.verbose { "Lazy verbose" }
            logger.debug { "Lazy debug" }
            logger.info { "Lazy info" }
            logger.warn { "Lazy warn" }
            logger.error { "Lazy error" }
            logger.fatal { "Lazy fatal" }
        }

        assertEquals(
            listOf(VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, INFO,
                VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT),
            Logging.history.map { it.level }
        )
        verify(telemetryLog).add("I | Test | Info")
    }

    @Test
    fun offEventsGoNowhere() {
        mockStatic(RobotLog::class.java).use {
            Logging.add("Test", OFF, "Hidden")
            Logging.writeToRobotLog(OFF, "Test", "Hidden")
        }

        assertTrue(Logging.history.isEmpty())
    }

    @Test
    fun levelsExposeIndicatorsAndThresholdBehavior() {
        assertEquals(listOf('V', 'D', 'I', 'W', 'E', 'A', '-'), LogLevel.entries.map { it.indicator })
        LogLevel.entries.forEach { threshold ->
            LogLevel.entries.forEach { level ->
                val expected = threshold != OFF && level != OFF && level.ordinal >= threshold.ordinal
                assertEquals(expected, threshold.accepts(level))
            }
        }
    }

    @Test
    fun logEntriesFormatAndSearchEveryField() {
        val entry = LogEntry(INFO, "Gate", "Idle", "Gate.open")
        val entryWithoutContext = LogEntry(INFO, "Gate", "Opened")

        assertEquals("I | Gate | Idle", entry.line)
        assertEquals("", entryWithoutContext.context)
        assertTrue(entry.matches("info gate idle gate.open"))
        assertTrue(entry.matches("GATE"))
        assertFalse(entry.matches("Deflector"))
        assertTrue(entryWithoutContext.matches("Opened"))
    }

    @Test
    fun everySeverityMapsToRobotLog() {
        mockStatic(RobotLog::class.java).use { robotLog ->
            Logging.writeToRobotLog(VERBOSE, "Tag", "Message")
            Logging.writeToRobotLog(DEBUG, "Tag", "Message")
            Logging.writeToRobotLog(INFO, "Tag", "Message")
            Logging.writeToRobotLog(WARN, "Tag", "Message")
            Logging.writeToRobotLog(ERROR, "Tag", "Message")
            Logging.writeToRobotLog(ASSERT, "Tag", "Message")

            robotLog.verify { RobotLog.vv("Tag", "Message") }
            robotLog.verify { RobotLog.dd("Tag", "Message") }
            robotLog.verify { RobotLog.ii("Tag", "Message") }
            robotLog.verify { RobotLog.ww("Tag", "Message") }
            robotLog.verify { RobotLog.ee("Tag", "Message") }
            robotLog.verify { RobotLog.aa("Tag", "Message") }
        }
    }
}
