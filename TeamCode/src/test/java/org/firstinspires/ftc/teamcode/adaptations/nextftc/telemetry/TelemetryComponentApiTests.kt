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

class TelemetryComponentApiTests {
    private lateinit var output: FtcTelemetry

    @Before
    fun setUp() {
        output = mock(FtcTelemetry::class.java)
        TelemetryComponent.output = output
        TelemetryComponent.bind(null)
        TelemetryComponent.LEVEL = VERBOSE
        TelemetryComponent.FILTER = ""
        TelemetryComponent.DISPLAY_FILTER = ""
        Logging.initialize()
        TelemetryComponent.beginFrame()
        clearInvocations(output)
    }

    @Test
    fun settingsArePanelsConfigurableWithoutExposingRuntimeState() {
        assertTrue(TelemetryComponent::class.java.isAnnotationPresent(Configurable::class.java))
        val configurable = TelemetryComponent::class.java.declaredFields
            .filter { !it.isSynthetic && !java.lang.reflect.Modifier.isFinal(it.modifiers) }
            .filterNot { it.isAnnotationPresent(IgnoreConfigurable::class.java) }
            .map { it.name }.toSet()

        assertEquals(setOf("LEVEL", "FILTER"), configurable)
    }

    @Test
    fun diagnosticsAreSampledEachFrameUnlessPanelsOverridesTheLevel() {
        val diagnostics = TestDiagnostics()
        TelemetryComponent.bind(diagnostics)
        assertEquals(INFO, TelemetryComponent.LEVEL)
        assertEquals("Gate", TelemetryComponent.DISPLAY_FILTER)

        diagnostics.level = DiagnosticsConfig.Level.WARN
        diagnostics.filter = "Deflector"
        TelemetryComponent.beginFrame()
        assertEquals(WARN, TelemetryComponent.LEVEL)
        assertEquals("Deflector", TelemetryComponent.DISPLAY_FILTER)

        TelemetryComponent.LEVEL = ERROR
        diagnostics.level = DiagnosticsConfig.Level.INFO
        TelemetryComponent.beginFrame()
        assertEquals(ERROR, TelemetryComponent.LEVEL)

        TelemetryComponent.bind(null)
        assertEquals(OFF, TelemetryComponent.LEVEL)
        assertEquals("", TelemetryComponent.DISPLAY_FILTER)
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

        verify(output).addLine(TelemetryComponent.title("TEL"))
        verify(output).addData("V | Gate | Verbose", 1 as Any)
        verify(output).addData("D | Gate | Debug", 2 as Any)
        verify(output).addData("I | Gate | Info", 3 as Any)
        verify(output).addData("W | Gate | Warn", 4 as Any)
        verify(output).addData("E | Gate | Error", 5 as Any)
        verify(output).addData("A | Gate | Fatal", 6 as Any)
        verify(output, times(1)).addLine(TelemetryComponent.title("TEL"))
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
        TelemetryComponent.DISPLAY_FILTER = "Gate"
        TelemetryComponent.FILTER = "Position"

        assertTrue(TelemetryComponent.add("Gate", INFO, "Position", 0.5))
        assertFalse(TelemetryComponent.add("Gate", INFO, "Reversed", true))
        assertFalse(TelemetryComponent.add("Deflector", INFO, "Position", 0.5))

        verify(output).addData("I | Gate | Position", 0.5 as Any)
    }

    @Test
    fun levelsFilterBeforeLazyValuesAreRead() {
        var evaluations = 0
        TelemetryComponent.LEVEL = INFO
        val tel = Tel("Gate")

        tel.debug("Hidden") { ++evaluations }
        tel.info("Visible") { ++evaluations }

        assertEquals(1, evaluations)
        verify(output, never()).addData("D | Gate | Hidden", 1 as Any)
        verify(output).addData("I | Gate | Visible", 1 as Any)
    }

    @Test
    fun configurationIsAlwaysVisibleAndStartsItsOwnSection() {
        TelemetryComponent.LEVEL = ASSERT
        TelemetryComponent.DISPLAY_FILTER = "Hidden"
        TelemetryComponent.FILTER = "Hidden"

        TelemetryComponent.config("Alliance", "BLUE")

        verify(output).addLine(TelemetryComponent.title("CONFIG"))
        verify(output).addData("Alliance", "BLUE" as Any)
    }

    @Test
    fun framesClearOutputAndSectionState() {
        TelemetryComponent.section("CONFIG")
        TelemetryComponent.beginFrame()
        TelemetryComponent.section("CONFIG")

        verify(output).clear()
        verify(output, times(2)).addLine(TelemetryComponent.title("CONFIG"))
        assertEquals(61, TelemetryComponent.title("CONFIG").length)
        assertTrue(with(TelemetryComponent) { "Deflector".matches("FLECT") })
        assertFalse(with(TelemetryComponent) { "Deflector".matches("Gate") })
    }
}
