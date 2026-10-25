package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import dev.nextftc.control.ControlSystem
import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import dev.nextftc.control.feedback.PIDCoefficients
import dev.nextftc.control.feedforward.BasicFeedforwardParameters
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad2
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sign
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.velocityPercentage
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

@Configurable
object Flywheel : Subsystem() {
    val PID = PIDCoefficients(1.0)
    val FF = BasicFeedforwardParameters(kV = 1.0)
    var AXIAL_KS = 0.0
    var AXIAL_KV = 0.0008
    var AXIAL_KA = 0.0
    var FWD = 0.4
    var REV = -0.5
    var HOLD = -0.2
    var STOP = 0.0
    var TOLERANCE = 0.05
    var VEL = STOP
    var targeting = false

    val motorLeft = MotorEx("flywheelLeft").floatMode().reversed()
    val motorRight = MotorEx("flywheelRight").floatMode()

    val leftController = controlSystem { velPid(PID); basicFF(FF) }
    val rightController = controlSystem { velPid(PID); basicFF(FF) }

    val launch by instant { targeting = true; VEL = FWD }
    val forward by instant { targeting = false; VEL = FWD }
    val reverse by instant { targeting = false; VEL = REV }
    val hold by instant { targeting = false; VEL = HOLD }
    val stop by instant { targeting = false; VEL = STOP }

    override fun initialize() {
        targeting = false
        VEL = STOP
        leftController.reset()
        rightController.reset()
    }

    override fun controls() {
        val triggers = (gamepad2.leftTrigger greaterThan 0.5) or
            (gamepad2.rightTrigger greaterThan 0.5)
        triggers whenBecomesTrue forward
        triggers whenBecomesFalse stop

        val flywheel = !gamepad2.start and gamepad2.b
        (flywheel and gamepad2.dpadUp) whenBecomesTrue forward
        (flywheel and gamepad2.dpadDown) whenBecomesTrue reverse
        (flywheel and (gamepad2.dpadLeft or gamepad2.dpadRight)) whenBecomesTrue stop
    }

    override fun periodic() {
        VEL = calculateVelocity().coerceIn(-1.0, 1.0)
        set(motorLeft, leftController)
        set(motorRight, rightController)
    }

    override fun stop() {
        initialize()
        motorLeft.power = 0.0
        motorRight.power = 0.0
    }

    fun calculateVelocity(): Double {
        if (!targeting) return VEL

        val distance = hypot(follower.pose.x - Nav.goal.x, follower.pose.y - Nav.goal.y)
        val axialVelocity = follower.velocity.xComponent
        val axialAcceleration = follower.acceleration.xComponent
        val feedforward = AXIAL_KS * axialVelocity.sign +
            AXIAL_KV * axialVelocity + AXIAL_KA * axialAcceleration

        return 1.064487 + (0.2797805 - 1.064487) /
            (1 + (distance / 177.5942).pow(3.400669)) + feedforward
    }

    fun ready(): Boolean {
        return VEL > 0.0 && abs(motorLeft.velocityPercentage - VEL) <= TOLERANCE &&
            abs(motorRight.velocityPercentage - VEL) <= TOLERANCE
    }

    fun untilReady() = WaitUntil(::ready).endAfter(1.0)

    fun set(motor: MotorEx, controller: ControlSystem) = motor.update {
        controller.goal = KineticState(velocity = VEL)
        power = if (VEL == 0.0) {
            controller.reset()
            0.0
        } else {
            controller.calculate(KineticState(velocity = velocityPercentage)).coerceIn(-1.0, 1.0)
        }
    }
}
