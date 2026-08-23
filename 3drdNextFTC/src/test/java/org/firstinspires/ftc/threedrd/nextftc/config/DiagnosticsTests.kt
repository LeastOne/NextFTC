package org.firstinspires.ftc.threedrd.nextftc.config

import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.OFF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DiagnosticsTests {
    open class Base(var filter: String = "Gate")
    class Config(var level: Diagnostics.Level = INFO) : Base()
    class Invalid(var level: String = "INFO", var filter: Int = 1)

    @Test
    fun cachesConventionalFieldsAndReadsTheirLiveValues() {
        val config = Config()
        val diagnostics = Diagnostics(config)

        assertNotNull(diagnostics.levelField)
        assertNotNull(diagnostics.filterField)
        assertEquals(INFO, diagnostics.level())
        assertEquals("Gate", diagnostics.filter())

        config.level = Diagnostics.Level.WARN
        config.filter = "Deflector"
        assertEquals(Diagnostics.Level.WARN, diagnostics.level())
        assertEquals("Deflector", diagnostics.filter())
        assertEquals(diagnostics.levelField, diagnostics.field("level"))
    }

    @Test
    fun missingOrIncorrectFieldsUseSafeDefaults() {
        listOf(Diagnostics(), Diagnostics(Any()), Diagnostics(Invalid())).forEach {
            assertEquals(OFF, it.level())
            assertEquals("", it.filter())
        }
    }

    @Test
    fun levelsMapToBothIndependentDestinations() {
        Diagnostics.Level.entries.forEach {
            assertEquals(it.name, it.toTelLevel().name)
            assertEquals(it.name, it.toLogLevel().name)
        }
    }
}
