package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.commands.utility.NullCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoTests {
    @Test
    fun executeCreatesANewNamedEntryCommand() {
        val first = Auto.execute()
        val second = Auto.execute()

        assertTrue(first is NullCommand)
        assertEquals("Auto.execute", first.name)
        assertNotSame(first, second)
    }
}
