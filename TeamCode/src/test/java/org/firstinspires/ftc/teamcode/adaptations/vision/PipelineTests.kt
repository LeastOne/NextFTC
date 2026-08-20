package org.firstinspires.ftc.teamcode.adaptations.vision

import org.junit.Assert.assertEquals
import org.junit.Test

class PipelineTests {
    @Test
    fun indexesMatchTheLimelightConfiguration() {
        assertEquals(listOf(0, 1, 2, 3), Pipeline.entries.map { it.index })
    }
}
