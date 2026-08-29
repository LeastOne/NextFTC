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
        assertTrue(Alliance.UNKNOWN(2).isNaN())
        assertEquals(+2.0, Alliance.BLUE(2), 0.0)
        assertEquals(-2.0, Alliance.RED(2), 0.0)
    }
}
