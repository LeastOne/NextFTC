package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.commands.groups.ParallelGroup
import dev.nextftc.core.commands.groups.CommandGroup
import dev.nextftc.core.commands.Command
import com.google.gson.JsonParser
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.ArgumentCaptor
import java.nio.file.Files
import org.firstinspires.ftc.teamcode.adaptations.quanomous.Quanomous
import org.firstinspires.ftc.teamcode.adaptations.quanomous.QuanomousFiles

class AutoTests {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { hardwareMap = mock(HardwareMap::class.java) }
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }
        component.preInit()
        Config.config.delay = 0.0
        Config.config.quanomous = null
        Quanomous.files = QuanomousFiles(Files.createTempDirectory("auto-quanomous").toFile())
    }

    @After
    fun tearDown() {
        component.postStop()
        Quanomous.files = QuanomousFiles()
    }

    @Test
    fun locateResetsPedrosStartingPoseExactly() {
        val pose = Nav.start
        Auto.locate.start()

        val poses = ArgumentCaptor.forClass(com.pedropathing.geometry.Pose::class.java)
        inOrder(follower).run {
            verify(follower).setStartingPose(poses.capture())
            verify(follower).setPose(poses.capture())
        }
        poses.allValues.forEach {
            assertEquals(pose.x, it.x, 0.0)
            assertEquals(pose.y, it.y, 0.0)
            assertEquals(pose.heading, it.heading, 0.0)
        }
        assertEquals("Auto.locate", Auto.locate.name)
    }

    @Test
    fun executeCreatesAComposableSampleRoutine() {
        val first = Auto.execute()
        val second = Auto.execute()

        assertNotSame(first, second)
        assertTrue(first.requirements.containsAll(
            listOf(Auto, Drive, Gate, Deflector, Intake, Conveyor, Flywheel)
        ))

        val commands = flatten(first)
        assertTrue(commands.contains(Auto.locate))
        assertTrue(commands.filterIsInstance<ParallelGroup>().size >= 2)
        assertTrue(commands.any { it.name.contains("Gate.open") })
        assertNotSame(Auto.sample(), Auto.sample())
        assertTrue(Auto.selected().requirements.contains(Gate))
    }

    @Test
    fun selectedQuanomousRoutineUsesTheRegisteredSeasonCommands() {
        val steps = JsonParser().parse("""[
            {"cmd":"delay","seconds":0.1},
            {"cmd":"intake"},
            {"cmd":"intake_gate"},
            {"cmd":"deposit"},
            {"cmd":"release"},
            {"cmd":"chase"},
            {"cmd":"park"},
            {"cmd":"drive","tx":1,"ty":2,"h":90},
            {"cmd":"score"},
            {"cmd":"gate","open":true},
            {"cmd":"gate","open":false},
            {"cmd":"deflector","up":true},
            {"cmd":"deflector","up":false}
        ]""").asJsonArray
        Quanomous.files.save("routine.json", steps)
        Config.config.quanomous = "routine.json"

        val selected = Auto.selected() as dev.nextftc.core.commands.groups.SequentialGroup

        assertEquals(13, selected.commands.size)
        assertTrue(selected.requirements.containsAll(listOf(Drive, Intake, Conveyor, Flywheel, Gate, Deflector)))
        assertTrue(Auto.stopAll().requirements.containsAll(listOf(Drive, Intake, Conveyor, Flywheel)))
    }

    fun flatten(command: Command): List<Command> = listOf(command) +
        ((command as? CommandGroup)?.commands?.flatMap(::flatten) ?: emptyList())
}
