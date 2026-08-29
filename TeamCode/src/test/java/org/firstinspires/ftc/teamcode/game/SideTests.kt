package org.firstinspires.ftc.teamcode.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SideTests {
    @Test
    fun signsDescribeFieldOrientation() {
        assertTrue(Side.UNKNOWN.sign.isNaN())
        assertEquals(+1.0, Side.NORTH.sign, 0.0)
        assertEquals(-1.0, Side.SOUTH.sign, 0.0)
        assertTrue(Side.UNKNOWN(2).isNaN())
        assertEquals(+2.0, Side.NORTH(2), 0.0)
        assertEquals(-2.0, Side.SOUTH(2), 0.0)
    }
}
