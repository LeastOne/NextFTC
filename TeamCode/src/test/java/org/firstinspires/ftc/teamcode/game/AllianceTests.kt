package org.firstinspires.ftc.teamcode.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AllianceTests {
    @Test
    fun signsDescribeFieldOrientation() {
        assertTrue(Alliance.UNKNOWN.sign.isNaN())
        assertEquals(+1.0, Alliance.BLUE.sign, 0.0)
        assertEquals(-1.0, Alliance.RED.sign, 0.0)
    }
}
