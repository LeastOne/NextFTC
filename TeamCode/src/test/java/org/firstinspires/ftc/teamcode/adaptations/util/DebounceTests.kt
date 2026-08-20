package org.firstinspires.ftc.teamcode.adaptations.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebounceTests {
    @Test
    fun acceptsOnlySeparatedRisingEdges() {
        var time = 0.0
        val debounce = Debounce { time }

        assertFalse(debounce.triggered(false, 0.1))
        time = 0.2
        assertTrue(debounce.triggered(true, 0.1))
        time = 0.4
        assertFalse(debounce.triggered(true, 0.1))
        assertFalse(debounce.triggered(false, 0.1))
        time = 0.6
        assertTrue(debounce.triggered(true, 0.1))

        debounce.reset()
        assertFalse(debounce.previous)
        assertFalse(debounce.triggered(true, 0.1))
    }
}
