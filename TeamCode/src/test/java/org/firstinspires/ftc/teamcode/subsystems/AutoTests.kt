package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.commands.groups.CommandGroup
import dev.nextftc.core.commands.Command
import com.google.gson.JsonParser
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.nio.file.Files
import org.firstinspires.ftc.threedrd.quanomous.Quanomous as QuanomousData
import org.firstinspires.ftc.threedrd.quanomous.QuanomousStorage

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
        QuanomousData.storage = QuanomousStorage(Files.createTempDirectory("auto-quanomous").toFile())
    }

    @After
    fun tearDown() {
        component.postStop()
        QuanomousData.storage = QuanomousStorage()
    }

    @Test
    fun executeFailsWithoutAQuanomousRoutine() {
        assertThrows(NullPointerException::class.java) { Auto.execute() }
        verify(follower, never()).setStartingPose(org.mockito.ArgumentMatchers.any())
        verify(follower, never()).setPose(org.mockito.ArgumentMatchers.any())
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
        QuanomousData.storage.save("routine.json", steps)
        Config.config.quanomous = "routine.json"

        val selected = Auto.execute()

        assertTrue(selected.requirements.containsAll(listOf(Drive, Intake, Conveyor, Flywheel, Gate, Vision)))
        assertTrue(flatten(selected).size > 8)
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
                org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial.CENTER,
                org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral.CENTER)
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
    fun parkWaitsUntilGoalLockIsReleased() {
        Drive.goalLocked = true
        assertFalse(Auto.canPark())
        assertFalse(Auto.park(false,
            org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial.CENTER,
            org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral.CENTER).isDone)
        Drive.goalLocked = false
        assertTrue(Auto.canPark())
        val wait = Auto.park(false,
            org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial.CENTER,
            org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral.CENTER)
            .let { it.commands.first() }
        wait.start()
        wait.isDone
        wait.stop(false)
    }

    fun flatten(command: Command): List<Command> = listOf(command) +
        ((command as? CommandGroup)?.commands?.flatMap(::flatten) ?: emptyList())
}
