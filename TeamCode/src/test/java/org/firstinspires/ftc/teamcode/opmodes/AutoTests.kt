package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class AutoTests {
    lateinit var component: PedroComponent

    @Before
    fun resetCommands() {
        CommandManager.cancelAll()
        CommandManager.run()
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { hardwareMap = mock(HardwareMap::class.java) }
        component = PedroComponent { mock(Follower::class.java) }
        component.preInit()
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()
        component.postStop()
    }

    @Test
    fun compositionCanBeCreated() {
        assertNotNull(Auto())
    }

    @Test
    fun schedulesTheAutonomousEntryCommand() {
        Auto().onStartButtonPressed()
        CommandManager.run()

        assertEquals(1, CommandManager.snapshot.size)
        assertTrue(CommandManager.snapshot.single().startsWith("SequentialGroup("))
    }
}
