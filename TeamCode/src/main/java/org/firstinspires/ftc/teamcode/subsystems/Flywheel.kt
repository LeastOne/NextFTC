package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.FLOAT
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad2
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sign
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.MotorEx
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.tel
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem

@Configurable
object Flywheel : Subsystem() {
    var PIDF = PIDFCoefficients(64.0, 0.0, 0.0, 8.0)
    var AXIAL_KS = 0.0
    var AXIAL_KV = 0.0008
    var AXIAL_KA = 0.0
    var FWD = 0.4
    var REV = -0.5
    var HOLD = -0.2
    var STOP = 0.0
    var THRESHOLD = 0.85
    var VEL = STOP
    var targeting = false

    val motorLeft = MotorEx("flywheelLeft") {
        zeroPowerBehavior = FLOAT
        direction = REVERSE
        mode = RUN_USING_ENCODER
    }

    val motorRight = MotorEx("flywheelRight") {
        zeroPowerBehavior = FLOAT
        direction = FORWARD
        mode = RUN_USING_ENCODER
    }

    val launch by instant { targeting = true; VEL = FWD }
    val forward by instant { targeting = false; VEL = FWD }
    val reverse by instant { targeting = false; VEL = REV }
    val hold by instant { targeting = false; VEL = HOLD }
    val stop by instant { targeting = false; VEL = STOP }

    override fun initialize() {
        targeting = false
        VEL = STOP
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
        VEL = calculateVelocity()
        set(motorLeft)
        set(motorRight)
    }

    override fun stop() {
        targeting = false
        VEL = STOP
        motorLeft.power = STOP
        motorRight.power = STOP
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
        val target = motorLeft.motor.motorType.achieveableMaxTicksPerSecond * VEL * THRESHOLD
        return motorLeft.motor.velocity >= target && motorRight.motor.velocity >= target
    }

    fun untilReady() = WaitUntil(::ready).endAfter(1.0)

    fun set(motor: MotorEx) {
        motor.motor.setVelocityPIDFCoefficients(PIDF.p, PIDF.i, PIDF.d, PIDF.f)
        motor.motor.velocity = motor.motor.motorType.achieveableMaxTicksPerSecond * VEL
        motor.tel()
    }
}
