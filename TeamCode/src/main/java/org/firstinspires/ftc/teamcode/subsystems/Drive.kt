package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.util.ElapsedTime
import dev.nextftc.bindings.Range
import dev.nextftc.control.ControlSystem
import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import dev.nextftc.control.feedback.AngleType.RADIANS
import dev.nextftc.control.feedback.PIDCoefficients
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.core.units.Distance
import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad1
import kotlin.math.abs
import kotlin.math.sign
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial
import org.firstinspires.ftc.threedrd.nextftc.subsystems.DriveSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral
import org.firstinspires.ftc.threedrd.pedropathing.PedroDriverControlled
import org.firstinspires.ftc.threedrd.pedropathing.axial
import org.firstinspires.ftc.threedrd.pedropathing.lateral
import org.firstinspires.ftc.threedrd.pedropathing.midpoint
import org.firstinspires.ftc.threedrd.pedropathing.tiles
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.side
import org.firstinspires.ftc.teamcode.subsystems.Config.state

@Configurable
object Drive : DriveSubsystem() {
    val FORWARD_PID = PIDCoefficients(0.035, 0.005, 0.005)
    val STRAFE_PID = PIDCoefficients(0.025, 0.005, 0.005)
    val HEADING_PID = PIDCoefficients(0.35, 0.0015, 0.05)
    var FORWARD_KS = 0.05
    var STRAFE_KS = 0.05
    var TURN_KS = 0.05
    var HEADING_KS = 0.0
    var HEADING_KV = 0.0
    var HEADING_KA = 0.0
    var ALLOWABLE_STILL = 2.0
    var POWER_INTAKE = 0.5
    var POWER_LOW = 0.5
    var POWER_MEDIUM = 0.75
    var POWER_HIGH = 1.0
    var POWER_AUTO = 1.0
    var TO_FAR = 3.tiles.inIn
    var GOAL_LOCK_MAX_TURN = 0.4

    val forwardController = controlSystem { posPid(FORWARD_PID) }
    val strafeController = controlSystem { posPid(STRAFE_PID) }
    val turnController = controlSystem { angular(RADIANS) { posPid(HEADING_PID) } }

    var goalLocked = false
    var chaseLocked = false
    val stillTimer = ElapsedTime()

    private lateinit var forwardInput: Range
    private lateinit var strafeInput: Range
    private lateinit var turnInput: Range

    val driverControlled = PedroDriverControlled(
        { -forwardInput.get() },
        { -strafeInput.get() },
        { -turnInput.get() },
        { config.robotCentric && !chaseLocked },
        { if (config.robotCentric || chaseLocked || alliance == UNKNOWN) 0.0
          else alliance(-90).deg.inRad }
    ).apply {
        drive = ::calculateForward
        strafe = ::calculateStrafe
        turn = ::calculateTurn
        requires(this@Drive)
    }

    val chaseControlled = PedroDriverControlled(
        { 0.0 }, { 0.0 }, { 0.0 }, { false }
    ).apply {
        drive = ::calculateForward
        strafe = ::calculateStrafe
        turn = ::calculateTurn
        requires(this@Drive)
        named("Drive.chaseControlled")
    }

    val intakePower by instant { driverControlled.scalar = POWER_INTAKE }
    val low by instant { driverControlled.scalar = POWER_LOW }
    val medium by instant { driverControlled.scalar = POWER_MEDIUM }
    val high by instant { driverControlled.scalar = POWER_HIGH }
    val autoPower by instant { driverControlled.scalar = POWER_AUTO }
    val toggleCentric by instant { config.robotCentric = !config.robotCentric }
    val goalLock by instant { setGoalLock(true) }
    val goalUnlock by instant { setGoalLock(false) }
    val chaseLock by instant { setChaseLock(true) }
    val chaseUnlock by instant { setChaseLock(false) }

    override val defaultCommand
        get() = if (state.teleop) driverControlled else super.defaultCommand

    override fun initialize() {
        forwardInput = gamepad1.leftStickY
        strafeInput = gamepad1.leftStickX
        turnInput = gamepad1.rightStickX
        driverControlled.scalar = if (state.auto) POWER_AUTO else POWER_HIGH
        setGoalLock(false)
        setChaseLock(false)
        stillTimer.reset()
    }

    override fun controls() {
        val driving = !gamepad1.back
        (driving and gamepad1.dpadDown) whenBecomesTrue low
        (driving and (gamepad1.dpadLeft or gamepad1.dpadRight)) whenBecomesTrue medium
        (driving and gamepad1.dpadUp) whenBecomesTrue high
        (gamepad1.back and gamepad1.start) whenBecomesTrue toggleCentric
    }

    override fun periodic() {
        tel.info("Power", "%.2f".format(driverControlled.scalar))
        tel.debug("X", "%.1f".format(follower.pose.x))
        tel.debug("Y", "%.1f".format(follower.pose.y))
        tel.debug("Heading (deg)", "%.1f".format(Math.toDegrees(follower.pose.heading)))
        tel.verbose("Busy", follower.isBusy)
        tel.verbose("Still", isStill(0.4))
        tel.verbose("Goal Lock", goalLocked)
        tel.verbose("Chase Lock", chaseLocked)
    }

    fun setGoalLock(enabled: Boolean) {
        goalLocked =
            state.started && !config.robotCentric &&
            alliance != UNKNOWN &&
            side != UNKNOWN_SIDE && enabled
    }

    fun setChaseLock(enabled: Boolean) {
        chaseLocked = enabled
        if (enabled) follower.startTeleopDrive()
    }

    fun calculateForward(input: Double): Double {
        if (!chaseLocked || Vision.element == null) return input
        val remaining = Nav.artifactForwardRemaining
        return if (abs(remaining) < 1) input
            else correction(forwardController, remaining, follower.velocity.xComponent, FORWARD_KS)
    }

    fun calculateStrafe(input: Double): Double {
        if (!chaseLocked || Vision.element == null) return input
        val remaining = Nav.artifactStrafeRemaining
        return if (abs(remaining) < 1) input
            else correction(strafeController, remaining, follower.velocity.yComponent, STRAFE_KS)
    }

    fun calculateTurn(input: Double): Double {
        val remaining = when {
            goalLocked -> Nav.goalHeadingRemaining
            chaseLocked && Vision.element != null -> Nav.artifactHeadingRemaining
            else -> return input
        }
        if (chaseLocked && abs(Math.toDegrees(remaining)) < 2) return input
        val correction = correction(turnController, remaining, follower.angularVelocity, TURN_KS)
            .coerceIn(-GOAL_LOCK_MAX_TURN, GOAL_LOCK_MAX_TURN)
        val velocity = follower.velocity.yComponent
        val feedforward = HEADING_KS * velocity.sign + HEADING_KV * velocity +
            HEADING_KA * follower.acceleration.yComponent
        return correction + feedforward
    }

    fun correction(controller: ControlSystem, remaining: Double, velocity: Double, kS: Double) =
        controller.calculate(KineticState(remaining, velocity)) - remaining.sign * kS

    fun isStill(): Boolean {
        val still = follower.acceleration.magnitude < ALLOWABLE_STILL
        if (!still) stillTimer.reset()
        return still
    }

    fun isStill(seconds: Double) = isStill() && stillTimer.seconds() > seconds
    fun isAtElement() = Vision.element == null || follower.pose.distanceFrom(Nav.artifact) < Vision.ELEMENT_RADIUS
    fun isTooFar(pose: Pose?) = state.teleop && pose != null && follower.pose.distanceFrom(pose) > TO_FAR
    fun until(pose: Pose, distance: Distance) = WaitUntil { follower.pose.distanceFrom(pose) < distance.inIn }
    fun untilHeading(degrees: Double) = WaitUntil { abs(Nav.goalHeadingRemaining) < degrees.deg.inRad }
    fun untilStill(seconds: Double) = WaitUntil { isStill(seconds) }
    fun untilAtElement() = WaitUntil(::isAtElement).thenWait(1.5)
    fun untilDepositNorth(distance: Distance) = WaitUntil {
        val remaining = follower.pose.distanceFrom(Nav.depositNorth())
        if (distance.inIn < 0) remaining < -distance.inIn else remaining > distance.inIn
    }

    val toSpike by deferred { index: Int ->
        when (index) {
            0 -> toSpike0
            1 -> toSpike1
            2 -> toSpike2
            3 -> toSpike3
            else -> error("Unknown spike: $index")
        }
    }

    val toSpike0 by deferred {
        curve(
            if (follower.pose.x > 1.tiles.inIn)
                Nav.spike0.axial(1.tiles).lateral(alliance(0.8).tiles)
            else Nav.spike0.axial((-3.25).tiles).lateral(alliance(2.15).tiles),
            if (follower.pose.x > 1.tiles.inIn)
                Nav.spike0.axial((-1).tiles).lateral(alliance(-0.2).tiles)
            else Nav.spike0.axial((-1.5).tiles).lateral(alliance(-0.5).tiles),
            Nav.spike0.axial(0.45.tiles)
        ).endAfter(3.0)
    }

    val toSpike1 by deferred {
        curve(
            Nav.spike1.axial(if (follower.pose.x > 1.tiles.inIn) (-0.9).tiles else (-1.85).tiles)
                .axial(if (abs(follower.pose.y) > 1.75.tiles.inIn) (-0.75).tiles else 0.inches)
                .lateral(if (follower.pose.x > 1.tiles.inIn) 0.inches else alliance(0.3).tiles),
            Nav.spike1.axial(1.4.tiles), holdEnd = false
        )
    }

    val toSpike2 by deferred {
        if (abs(follower.pose.y) < 1.75.tiles.inIn) curve(
            Nav.spike2.axial((-1.1).tiles).lateral(alliance(follower.pose.x.sign * -0.3).tiles),
            Nav.spike2.axial(1.3.tiles), holdEnd = false
        ) else curve(
            Nav.spike2.axial((-0.4).tiles).lateral(alliance(-0.7).tiles),
            Nav.spike2.axial((-0.4).tiles).lateral(alliance(0.2).tiles),
            Nav.spike2.axial(1.3.tiles), holdEnd = false
        )
    }

    val toSpike3 by deferred {
        if (abs(follower.pose.y) < 1.75.tiles.inIn) curve(
            Nav.spike3.axial((-1.5).tiles).lateral(alliance(follower.pose.x.sign * -0.2).tiles),
            Nav.spike3.axial(1.tiles), holdEnd = false
        ) else curve(
            Nav.spike3.axial((-0.4).tiles).lateral(alliance(0.7).tiles),
            Nav.spike3.axial((-0.4).tiles).lateral(alliance(-0.2).tiles),
            Nav.spike3.axial(1.tiles), holdEnd = false
        )
    }

    val toDeposit by deferred(0.inches, 0.inches) {
        side: Side, axial: Distance, lateral: Distance -> (
            if (side == NORTH) medium.then(
                curve(Nav.depositNorth(axial, lateral)), high
            ) else {
                curve(
                    if (follower.pose.x > -1.tiles.inIn)
                        Pose(follower.pose.x, alliance(-0.5))
                    else follower.pose.midpoint(Nav.depositSouth(axial, lateral)),
                    Nav.depositSouth(axial, lateral)
                )
            }
        )
    }

    val toGate by deferred {
        curve(
            Nav.gate.axial(-1.75.tiles),
            Nav.gate,
            holdEnd = false
        )
    }

    val toGateIntake by deferred {
        curve(
            Nav.gateIntake.axial(-0.1.tiles).lateral(alliance(-0.3).tiles),
            Nav.gateIntake
        )
    }

    val toGateIntakeDepart by deferred {
        curve(
            Nav.gateIntakeDepart.axial(side(-0.05).tiles).lateral(-0.1.tiles),
            Nav.gateIntakeDepart,
            holdEnd = false
        )
    }

    val toBase by deferred {
        curve(Nav.base)
    }

    val toParking by deferred {
        gate: Boolean, axial: Axial, lateral: Lateral -> curve(
            Nav.parking(gate, axial, lateral)
        )
    }

    val toChaseScan by deferred {
        curve(Nav.chaseScan)
    }

    val toChase by deferred {
        count: Int -> curve(
            Nav.chase(count).axial(-1.25.tiles),
            Nav.chase(count)
        )
    }

    val toScore by deferred {
        curve(
            Nav.score.axial(-0.5.tiles).lateral(0.25.tiles),
            Nav.score
        )
    }

    val toPark by deferred {
        curve(Nav.park)
    }
}
