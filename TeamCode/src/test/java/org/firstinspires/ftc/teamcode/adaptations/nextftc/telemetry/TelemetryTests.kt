package org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry

import com.bylazar.configurables.annotations.Configurable
import com.bylazar.configurables.annotations.IgnoreConfigurable
import org.firstinspires.ftc.robotcore.external.Telemetry as FtcTelemetry
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.DiagnosticsConfig
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.ASSERT
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.DEBUG
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.ERROR
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.OFF
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.WARN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class TelemetryTests {
    private lateinit var output: FtcTelemetry

    @Before
    fun setUp() {
        output = mock(FtcTelemetry::class.java)
        Telemetry.output = output
        Telemetry.bind(null)
        Telemetry.LEVEL = VERBOSE
        Telemetry.FILTER = ""
        Telemetry.DISPLAY_FILTER = ""
        Logging.initialize()
        Telemetry.beginFrame()
        clearInvocations(output)
    }

    @Test
    fun settingsArePanelsConfigurableWithoutExposingRuntimeState() {
        assertTrue(Telemetry::class.java.isAnnotationPresent(Configurable::class.java))
        val configurable = Telemetry::class.java.declaredFields
            .filter { !it.isSynthetic && !java.lang.reflect.Modifier.isFinal(it.modifiers) }
            .filterNot { it.isAnnotationPresent(IgnoreConfigurable::class.java) }
            .map { it.name }.toSet()

        assertEquals(setOf("LEVEL", "FILTER"), configurable)
    }

    @Test
    fun diagnosticsAreSampledEachFrameUnlessPanelsOverridesTheLevel() {
        val diagnostics = TestDiagnostics()
        Telemetry.bind(diagnostics)
        assertEquals(INFO, Telemetry.LEVEL)
        assertEquals("Gate", Telemetry.DISPLAY_FILTER)

        diagnostics.level = DiagnosticsConfig.Level.WARN
        diagnostics.filter = "Deflector"
        Telemetry.beginFrame()
        assertEquals(WARN, Telemetry.LEVEL)
        assertEquals("Deflector", Telemetry.DISPLAY_FILTER)

        Telemetry.LEVEL = ERROR
        diagnostics.level = DiagnosticsConfig.Level.INFO
        Telemetry.beginFrame()
        assertEquals(ERROR, Telemetry.LEVEL)

        Telemetry.bind(null)
        assertEquals(OFF, Telemetry.LEVEL)
        assertEquals("", Telemetry.DISPLAY_FILTER)
    }

    private class TestDiagnostics(
        override var level: DiagnosticsConfig.Level = DiagnosticsConfig.Level.INFO,
        override var filter: String = "Gate"
    ) : DiagnosticsConfig

    @Test
    fun telConvenienceMethodsProduceLevelledCurrentState() {
        val tel = Tel("Gate")
        assertEquals("Gate", tel.source)

        tel.verbose("Verbose", 1)
        tel.debug("Debug", 2)
        tel.info("Info", 3)
        tel.warn("Warn", 4)
        tel.error("Error", 5)
        tel.fatal("Fatal", 6)
        tel.add(INFO, "Added") { 7 }
        tel.verbose("Lazy verbose") { 8 }
        tel.debug("Lazy debug") { 9 }
        tel.info("Lazy info") { 10 }
        tel.warn("Lazy warn") { 11 }
        tel.error("Lazy error") { 12 }
        tel.fatal("Lazy fatal") { 13 }

        verify(output).addLine(Telemetry.title("TEL"))
        verify(output).addData("V | Gate | Verbose", 1 as Any)
        verify(output).addData("D | Gate | Debug", 2 as Any)
        verify(output).addData("I | Gate | Info", 3 as Any)
        verify(output).addData("W | Gate | Warn", 4 as Any)
        verify(output).addData("E | Gate | Error", 5 as Any)
        verify(output).addData("A | Gate | Fatal", 6 as Any)
        verify(output, times(1)).addLine(Telemetry.title("TEL"))
    }

    @Test
    fun levelsExposeIndicatorsAndThresholdBehavior() {
        assertEquals(listOf('V', 'D', 'I', 'W', 'E', 'A', '-'), Level.entries.map { it.indicator })
        Level.entries.forEach { threshold ->
            Level.entries.forEach { level ->
                val expected = threshold != OFF && level != OFF && level.ordinal >= threshold.ordinal
                assertEquals(expected, threshold.accepts(level))
            }
        }
    }

    @Test
    fun commonAndSpecificFiltersMustBothMatch() {
        Telemetry.DISPLAY_FILTER = "Gate"
        Telemetry.FILTER = "Position"

        assertTrue(Telemetry.add("Gate", INFO, "Position", 0.5))
        assertFalse(Telemetry.add("Gate", INFO, "Reversed", true))
        assertFalse(Telemetry.add("Deflector", INFO, "Position", 0.5))

        verify(output).addData("I | Gate | Position", 0.5 as Any)
    }

    @Test
    fun levelsFilterBeforeLazyValuesAreRead() {
        var evaluations = 0
        Telemetry.LEVEL = INFO
        val tel = Tel("Gate")

        tel.debug("Hidden") { ++evaluations }
        tel.info("Visible") { ++evaluations }

        assertEquals(1, evaluations)
        verify(output, never()).addData("D | Gate | Hidden", 1 as Any)
        verify(output).addData("I | Gate | Visible", 1 as Any)
    }

    @Test
    fun configurationIsAlwaysVisibleAndStartsItsOwnSection() {
        Telemetry.LEVEL = ASSERT
        Telemetry.DISPLAY_FILTER = "Hidden"
        Telemetry.FILTER = "Hidden"

        Telemetry.config("Alliance", "BLUE")

        verify(output).addLine(Telemetry.title("CONFIG"))
        verify(output).addData("Alliance", "BLUE" as Any)
    }

    @Test
    fun framesClearOutputAndSectionState() {
        Telemetry.section("CONFIG")
        Telemetry.beginFrame()
        Telemetry.section("CONFIG")

        verify(output).clear()
        verify(output, times(2)).addLine(Telemetry.title("CONFIG"))
        assertEquals(61, Telemetry.title("CONFIG").length)
        assertTrue(with(Telemetry) { "Deflector".matches("FLECT") })
        assertFalse(with(Telemetry) { "Deflector".matches("Gate") })
    }
}
