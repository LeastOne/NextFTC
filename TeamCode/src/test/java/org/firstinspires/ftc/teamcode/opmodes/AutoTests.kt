package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.core.commands.*
import org.firstinspires.ftc.teamcode.subsystems.Auto.execute
import org.junit.*
import org.junit.Assert.*
import com.pedropathing.follower.Follower
import com.google.gson.JsonParser
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.mockito.Mockito.mock
import java.nio.file.Files
import org.firstinspires.ftc.threedrd.quanomous.Quanomous as QuanomousData
import org.firstinspires.ftc.threedrd.quanomous.QuanomousStorage
import org.firstinspires.ftc.teamcode.subsystems.Config

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
        QuanomousData.storage = QuanomousStorage(Files.createTempDirectory("auto-opmode").toFile())
        QuanomousData.storage.save("routine.json", JsonParser().parse(
            """[{"cmd":"delay","seconds":0.1}]"""
        ).asJsonArray)
        Config.quanomous = "routine.json"
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()
        component.postStop()
        QuanomousData.storage = QuanomousStorage()
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
