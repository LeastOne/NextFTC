package org.firstinspires.ftc.teamcode.adaptations.nextftc.commands

import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class InstantCommandTests {
    private object TestSubsystem : Subsystem()

    @Test
    fun retainsItsOwnerAndAction() {
        var ran = false
        val action = Runnable { ran = true }
        val command = InstantCommand(TestSubsystem, action)

        assertSame(TestSubsystem, command.owner)
        assertSame(action, command.action)
        assertTrue(command.requirements.contains(TestSubsystem))

        command.start()
        assertTrue(ran)
    }
}
