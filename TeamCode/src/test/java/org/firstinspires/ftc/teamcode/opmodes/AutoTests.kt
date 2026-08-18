package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.core.commands.CommandManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AutoTests {
    @Before
    @After
    fun resetCommands() {
        CommandManager.cancelAll()
        CommandManager.run()
    }

    @Test
    fun compositionCanBeCreated() {
        assertNotNull(Auto())
    }

    @Test
    fun schedulesTheAutonomousEntryCommand() {
        Auto().onStartButtonPressed()
        CommandManager.run()

        assertEquals(listOf("Auto.execute"), CommandManager.snapshot)
    }
}
