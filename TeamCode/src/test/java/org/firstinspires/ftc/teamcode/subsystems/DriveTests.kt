package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Curve
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.Path
import com.pedropathing.paths.PathBuilder
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.bindings.Variable
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.NullCommand
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.ftc.Gamepads.gamepad1
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.TelemetryComponent as TeamTelemetry
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.pct
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.pctT
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.axial
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.lateral
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.firstinspires.ftc.teamcode.subsystems.Drive.POWER_HIGH
import org.firstinspires.ftc.teamcode.subsystems.Drive.POWER_LOW
import org.firstinspires.ftc.teamcode.subsystems.Drive.POWER_MEDIUM
import org.firstinspires.ftc.teamcode.subsystems.Drive.controls
import org.firstinspires.ftc.teamcode.subsystems.Drive.driverControlled
import org.firstinspires.ftc.teamcode.subsystems.Drive.follow
import org.firstinspires.ftc.teamcode.subsystems.Drive.high
import org.firstinspires.ftc.teamcode.subsystems.Drive.hold
import org.firstinspires.ftc.teamcode.subsystems.Drive.initialize
import org.firstinspires.ftc.teamcode.subsystems.Drive.low
import org.firstinspires.ftc.teamcode.subsystems.Drive.medium
import org.firstinspires.ftc.teamcode.subsystems.Drive.periodic
import org.firstinspires.ftc.teamcode.subsystems.Drive.stop
import org.firstinspires.ftc.teamcode.subsystems.Drive.toggleCentric
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties

class DriveTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent
    lateinit var builder: PathBuilder
    lateinit var chain: PathChain

    @Before
    fun setUp() {
        CommandManager.cancelAll()
        CommandManager.run()
        follower = mock(Follower::class.java)
        builder = mock(PathBuilder::class.java)
        chain = mock(PathChain::class.java)
        `when`(follower.pathBuilder()).thenReturn(builder)
        `when`(builder.addPath(any(Curve::class.java))).thenReturn(builder)
        `when`(builder.setLinearHeadingInterpolation(anyDouble(), anyDouble(), anyDouble())).thenReturn(builder)
        `when`(builder.build()).thenReturn(chain)
        component = PedroComponent { follower }
        component.preInit()
        POWER_LOW = 0.5
        POWER_MEDIUM = 0.75
        POWER_HIGH = 1.0
        config.robotCentric = false
        state.started = false
        state.teleop = false
        driverControlled.scalar = POWER_HIGH
        controls()
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()
        component.postStop()
    }

    @Test
    fun settingsArePanelsConfigurableWithoutExposingState() {
        assertTrue(Drive::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(
            setOf("POWER_LOW", "POWER_MEDIUM", "POWER_HIGH"),
            Drive::class.memberProperties
                .filterIsInstance<KMutableProperty<*>>()
                .filter { it.visibility == PUBLIC }
                .map { it.name }
                .toSet()
        )
    }

    @Test
    fun controlsInitializeAtHighPower() {
        POWER_HIGH = 0.9

        initialize()

        assertEquals(0.9, driverControlled.scalar, 0.0)
    }

    @Test
    fun driverInputsUsePedroSignConvention() {
        ActiveOpMode.it!!.gamepad1 = Gamepad()
        ActiveOpMode.it!!.gamepad1.left_stick_y = 0.25f
        ActiveOpMode.it!!.gamepad1.left_stick_x = -0.5f
        ActiveOpMode.it!!.gamepad1.right_stick_x = 0.75f
        config.robotCentric = true

        initialize()
        listOf("forwardInput", "strafeInput", "turnInput").forEach { name ->
            Drive::class.java.getDeclaredField(name).apply { isAccessible = true }
                .get(Drive).let { (it as Variable<*>).update() }
        }
        driverControlled.update()

        verify(follower).setTeleOpDrive(-0.25, 0.5, -0.75, true, 0.0)

        config.robotCentric = false
        driverControlled.update()

        verify(follower).setTeleOpDrive(-0.25, 0.5, -0.75, false, 0.0)
    }

    @Test
    fun powerCommandsUseCurrentSettingsWithoutInterruptingDrive() {
        POWER_LOW = 0.4
        POWER_MEDIUM = 0.6
        POWER_HIGH = 0.8

        low.start()
        assertEquals(0.4, driverControlled.scalar, 0.0)
        medium.start()
        assertEquals(0.6, driverControlled.scalar, 0.0)
        high.start()
        assertEquals(0.8, driverControlled.scalar, 0.0)
        assertTrue(low.requirements.contains(Drive))
        assertEquals("Drive.low", low.name)
        assertEquals("Drive.medium", medium.name)
        assertEquals("Drive.high", high.name)
    }

    @Test
    fun toggleChangesTheConfiguredDriveMode() {
        assertFalse(driverControlled.robotCentric())

        toggleCentric.start()
        assertTrue(config.robotCentric)
        assertTrue(driverControlled.robotCentric())

        toggleCentric.start()
        assertFalse(config.robotCentric)
        assertFalse(driverControlled.robotCentric())
        assertEquals("Drive.toggleCentric", toggleCentric.name)
        assertTrue(toggleCentric.requirements.contains(Drive))
    }

    @Test
    fun providesDriverControlAsTheTeleopDefault() {
        state.teleop = false
        assertTrue(Drive.defaultCommand is NullCommand)

        controls()
        CommandManager.run()
        assertFalse(CommandManager.hasCommandsUsing(Drive))
        verify(follower, never()).startTeleopDrive()

        state.teleop = true
        assertSame(driverControlled, Drive.defaultCommand)
        controls()
        CommandManager.run()
        assertFalse(CommandManager.hasCommandsUsing(Drive))
        verify(follower, never()).startTeleopDrive()
    }

    @Test
    fun pathCommandsRequireDriveAndFollowPedroPaths() {
        val start = Pose(1.0, 2.0, 0.25)
        val end = Pose(3.0, 4.0, 1.25)
        val path = Path(BezierLine(start, end))
        val chain = PathChain(path)

        val pathCommand = follow(path)
        val chainCommand = follow(chain)

        assertTrue(pathCommand.requirements.contains(Drive))
        assertTrue(chainCommand.requirements.contains(Drive))
        pathCommand.start()
        chainCommand.start()
        val paths = ArgumentCaptor.forClass(PathChain::class.java)
        verify(follower, times(2)).followPath(paths.capture())
        assertSame(path, paths.allValues[0].firstPath())
        assertSame(path, paths.allValues[1].firstPath())

        clearInvocations(follower)
        follow(path, false, 0.5).start()
        follow(chain, false, 0.5).start()
        verify(follower, times(2)).followPath(org.mockito.ArgumentMatchers.any(PathChain::class.java), org.mockito.ArgumentMatchers.eq(0.5), org.mockito.ArgumentMatchers.eq(false))
        verify(follower).followPath(chain, 0.5, false)
    }

    @Test
    fun toBuildsAPathFromTheCurrentPose() {
        val start = Pose(1.0, 2.0, 0.25)
        val end = Pose(3.0, 4.0, 1.25)
        `when`(follower.pose).thenReturn(start)

        val command = Drive.to(end)

        assertTrue(command.requirements.contains(Drive))
        command.start()
        val curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        val points = curve.value.controlPoints
        assertEquals(start, points.first())
        assertEquals(end, points.last())
        verify(follower).followPath(chain, true)
        assertEquals("Drive.to", command.name)
    }

    @Test
    fun sampleAutonomousCommandsHideTheirPathComposition() {
        val start = Pose(0.0, 0.0, 0.0)
        `when`(follower.pose).thenReturn(start)

        val toScore = Drive.toScore
        toScore.start()
        var curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        val approach = Nav.score.axial(-0.5.tiles).lateral(0.25.tiles)
        assertEquals(approach.x, curve.value.controlPoints[1].x, 0.0)
        assertEquals(approach.y, curve.value.controlPoints[1].y, 0.0)
        assertEquals(approach.heading, curve.value.controlPoints[1].heading, 0.0)
        assertEquals(Nav.score, curve.value.controlPoints.last())
        assertEquals("Drive.toScore", toScore.name)
        toScore.stop(false)

        clearInvocations(builder, follower)
        val toPark = Drive.toPark
        toPark.start()
        curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        assertEquals(Nav.park, curve.value.controlPoints.last())
        assertEquals("Drive.toPark", toPark.name)
        toPark.stop(false)
    }

    @Test
    fun curveAndCurvesBuildReusablePedroPaths() {
        val start = Pose(0.0, 0.0, 0.0)
        val middle = Pose(10.0, 5.0, 0.5)
        val end = Pose(20.0, 10.0, 1.0)
        `when`(follower.pose).thenReturn(start)

        val curve = Drive.curve(end, holdEnd = false)
        curve.start()
        var paths = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(paths.capture())
        assertEquals(3, paths.value.controlPoints.size)
        verify(follower).followPath(chain, false)
        assertEquals("Drive.curve", curve.name)

        clearInvocations(builder, follower)
        val multiPointCurve = Drive.curve(middle, end)
        multiPointCurve.start()
        paths = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(paths.capture())
        assertEquals(listOf(start, middle, end), paths.value.controlPoints)

        clearInvocations(builder, follower)
        val curves = Drive.curves(middle, end)
        curves.start()
        verify(builder, times(2)).addPath(any(Curve::class.java))
        verify(follower).followPath(chain, true)
        assertEquals("Drive.curves", curves.name)
    }

    @Test
    fun holdAndStopCommandsControlPedro() {
        val pose = Pose(1.0, 2.0, 0.5)
        `when`(follower.pose).thenReturn(pose)

        val paths = Drive.paths { addPath(BezierLine(follower.pose, Pose())) }
        paths.start()
        assertEquals("Drive.paths", paths.name)

        hold.start()
        verify(follower).holdPoint(pose)
        stop.start()
        verify(follower).breakFollowing()
    }

    @Test
    fun relativeMovementCommandsUseThePoseAtExecutionTime() {
        `when`(follower.pose).thenReturn(Pose(10.0, 20.0, Math.PI / 2))

        val forward = Drive.forward(5.0)
        forward.start()
        var curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        assertEquals(10.0, curve.value.controlPoints.last().x, 0.0001)
        assertEquals(25.0, curve.value.controlPoints.last().y, 0.0001)
        assertEquals("Drive.forward", forward.name)

        clearInvocations(builder, follower)
        val strafe = Drive.strafe(5.0)
        strafe.start()
        curve = ArgumentCaptor.forClass(Curve::class.java)
        verify(builder).addPath(curve.capture())
        assertEquals(5.0, curve.value.controlPoints.last().x, 0.0001)
        assertEquals(20.0, curve.value.controlPoints.last().y, 0.0001)
        assertEquals("Drive.strafe", strafe.name)

        val turn = Drive.turn(90.0)
        assertTrue(turn.requirements.contains(Drive))
        assertEquals("Drive.turn", turn.name)
        turn.start()
        verify(follower).turn(Math.PI / 2, true)
    }

    @Test
    fun progressCommandsWaitForPositiveAndEndRelativeThresholds() {
        val distance = Drive.until(10.inches)
        `when`(follower.distanceTraveledOnPath).thenReturn(9.0, 10.0)
        assertFalse(distance.isDone)
        assertTrue(distance.isDone)

        val remaining = Drive.until((-10).inches)
        `when`(follower.distanceRemaining).thenReturn(10.0, 9.0)
        assertFalse(remaining.isDone)
        assertTrue(remaining.isDone)

        val completion = Drive.until(75.pct)
        `when`(follower.pathCompletion).thenReturn(0.74, 0.75)
        assertFalse(completion.isDone)
        assertTrue(completion.isDone)

        val completionFromEnd = Drive.until((-25).pct)
        `when`(follower.pathCompletion).thenReturn(0.75, 0.74)
        assertFalse(completionFromEnd.isDone)
        assertTrue(completionFromEnd.isDone)

        val t = Drive.until(50.pctT)
        `when`(follower.currentTValue).thenReturn(0.49, 0.5)
        assertFalse(t.isDone)
        assertTrue(t.isDone)

        val tFromEnd = Drive.until((-50).pctT)
        `when`(follower.currentTValue).thenReturn(0.5, 0.49)
        assertFalse(tFromEnd.isDone)
        assertTrue(tFromEnd.isDone)

        val idle = Drive.untilNotBusy()
        `when`(follower.isBusy).thenReturn(true, false)
        assertFalse(idle.isDone)
        assertTrue(idle.isDone)
    }

    @Test
    fun periodicUpdatesPoseAndTelemetry() {
        val pose = Pose(12.34, 56.78, Math.toRadians(89.94))
        `when`(follower.pose).thenReturn(pose)
        `when`(follower.isBusy).thenReturn(true)
        TeamTelemetry.LEVEL = VERBOSE
        clearInvocations(ActiveOpMode.telemetry)

        periodic()

        verify(ActiveOpMode.telemetry).addData("I | Drive | Power", "1.00" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | X", "12.3" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | Y", "56.8" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Drive | Heading (deg)", "89.9" as Any)
        verify(ActiveOpMode.telemetry).addData("V | Drive | Busy", true as Any)
    }

    @Test
    fun periodicRetainsDriverCommandWhenDriveModeChanges() {
        `when`(follower.pose).thenReturn(Pose())
        val original = driverControlled
        config.robotCentric = true

        periodic()
        assertSame(original, driverControlled)
    }
}
