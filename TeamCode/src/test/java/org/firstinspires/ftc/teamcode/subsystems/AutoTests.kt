package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.commands.groups.ParallelGroup
import dev.nextftc.core.commands.groups.CommandGroup
import dev.nextftc.core.commands.Command
import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
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
            {"cmd":"intake","spike":1},
            {"cmd":"intake_gate"},
            {"cmd":"deposit","locale":"near","txo":0.1,"tyo":-0.2},
            {"cmd":"release"},
            {"cmd":"chase","cycles":2},
            {"cmd":"park","gate":true,"axial":"front","lateral":"right"},
            {"cmd":"drive","tx":1,"ty":2,"h":90,"axial":"back","lateral":"left"}
        ]""").asJsonArray
        Quanomous.files.save("routine.json", steps)
        Config.config.quanomous = "routine.json"

        val selected = Auto.selected() as dev.nextftc.core.commands.groups.SequentialGroup

        assertEquals(8, selected.commands.size)
        assertTrue(selected.requirements.containsAll(listOf(Drive, Intake, Conveyor, Flywheel, Gate, Vision)))
        assertTrue(Auto.stopAll().requirements.containsAll(listOf(Drive, Intake, Conveyor, Flywheel)))
    }

    @Test
    fun composesTheCompleteAutonomousVocabulary() {
        Config.config.alliance = org.firstinspires.ftc.teamcode.game.Alliance.RED
        Config.config.side = org.firstinspires.ftc.teamcode.game.Side.NORTH
        `when`(follower.pose).thenReturn(com.pedropathing.geometry.Pose(60.0, -20.0))

        listOf(
            Auto.intakeStop(), Auto.depositStart(), Auto.depositStop(),
            Auto.releaseGate(), Auto.gateIntake(), Auto.goalLock(true), Auto.goalLock(false),
            Auto.drive(Nav.score), Auto.chase(1), Auto.chaseCycle(), Auto.chaseIntake(),
            Auto.chaseComplete(), Auto.chaseDeposit(),
            Auto.park(true,
                org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.CENTER,
                org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.CENTER)
        ).forEach { assertTrue(it.requirements.isNotEmpty() || it.name.isNotEmpty()) }

        listOf(
            Auto.deposit(org.firstinspires.ftc.teamcode.game.Side.NORTH),
            Auto.deposit(org.firstinspires.ftc.teamcode.game.Side.SOUTH)
        ).forEach { it.start(); it.stop(true) }
        `when`(follower.pose).thenReturn(com.pedropathing.geometry.Pose(0.0, -20.0))
        Auto.deposit(org.firstinspires.ftc.teamcode.game.Side.SOUTH).run { start(); stop(true) }
        `when`(follower.pose).thenReturn(com.pedropathing.geometry.Pose(-60.0, -20.0))
        Auto.deposit(org.firstinspires.ftc.teamcode.game.Side.SOUTH).run { start(); stop(true) }

        val remaining = Auto.remaining(dev.nextftc.core.commands.utility.NullCommand())
        remaining.start()
        remaining.stop(true)
    }

    @Test
    fun parsesOptionalQuanomousArguments() {
        val empty = JsonObject()
        assertFalse(with(Auto) { empty.boolean("gate") })
        assertEquals("center", with(Auto) { empty.text("axial") })
        assertEquals("other", with(Auto) { empty.text("missing", "other") })
        val nullableText = mock(JsonObject::class.java)
        val nullableElement = mock(JsonElement::class.java)
        `when`(nullableText.get("value")).thenReturn(nullableElement)
        `when`(nullableElement.asString).thenReturn(null)
        assertEquals("fallback", with(Auto) { nullableText.text("value", "fallback") })
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.CENTER,
            with(Auto) { empty.axial() })
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.CENTER,
            with(Auto) { empty.lateral() })

        fun options(axial: String, lateral: String) = JsonObject().apply {
            addProperty("gate", true)
            addProperty("axial", axial)
            addProperty("lateral", lateral)
        }
        val frontLeft = options("FRONT", "LEFT")
        assertTrue(with(Auto) { frontLeft.boolean("gate") })
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.FRONT,
            with(Auto) { frontLeft.axial() })
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.LEFT,
            with(Auto) { frontLeft.lateral() })
        val backRight = options("back", "right")
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.BACK,
            with(Auto) { backRight.axial() })
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.RIGHT,
            with(Auto) { backRight.lateral() })

        val variants = JsonParser().parse("""[
            {"cmd":"deposit","locale":"far","txo":0,"tyo":0},
            {"cmd":"chase","cycles":0}
        ]""").asJsonArray
        assertEquals(2, Auto.programs.create(variants)
            .let { it as dev.nextftc.core.commands.groups.SequentialGroup }.commands.size)

        Drive.goalLocked = true
        assertFalse(Auto.canPark())
        assertFalse(Auto.park(false,
            org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.CENTER,
            org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.CENTER).isDone)
        Drive.goalLocked = false
        assertTrue(Auto.canPark())
        val wait = Auto.park(false,
            org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.CENTER,
            org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.CENTER)
            .let { it.commands.first() }
        wait.start()
        wait.isDone
        wait.stop(false)
    }

    fun flatten(command: Command): List<Command> = listOf(command) +
        ((command as? CommandGroup)?.commands?.flatMap(::flatten) ?: emptyList())
}
