package org.firstinspires.ftc.threedrd.pedropathing

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTests {
    @Test
    fun percentagesConvertToPedroProgressValues() {
        assertEquals(0.75, 75.pct.value, 0.0)
        assertEquals(0.5, 50.pctT.value, 0.0)
    }
}
