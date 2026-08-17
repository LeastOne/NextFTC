package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticsConfigTests {
    @Test
    fun levelsMapToBothIndependentDestinations() {
        DiagnosticsConfig.Level.entries.forEach {
            assertEquals(it.name, it.toTelLevel().name)
            assertEquals(it.name, it.toLogLevel().name)
        }
    }
}
