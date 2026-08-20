package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.FLOAT
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad2
import kotlin.math.hypot
import kotlin.math.pow
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.MotorEx
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.update
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem

@Configurable
object Conveyor : Subsystem() {
    var STOP = 0.0
    var FWD = 0.5
    var REV = -0.2
    var VEL = STOP

    val motor = MotorEx("conveyor") {
        mode = RUN_USING_ENCODER
        zeroPowerBehavior = FLOAT
    }

    val launch by instant { VEL = calculateVelocity() }
    val forward by instant { VEL = FWD }
    val reverse by instant { VEL = REV }
    val stop by instant { VEL = STOP }

    override fun initialize() {
        VEL = STOP
    }

    override fun controls() {
        val conveyor = !gamepad2.start and gamepad2.x
        (conveyor and gamepad2.dpadUp) whenBecomesTrue forward
        (conveyor and gamepad2.dpadDown) whenBecomesTrue reverse
        (conveyor and (gamepad2.dpadLeft or gamepad2.dpadRight)) whenBecomesTrue stop
    }

    override fun periodic() {
        motor.update { velocityPercentage = VEL }
    }

    override fun stop() {
        VEL = STOP
        motor.power = STOP
    }

    fun stopped() = VEL == STOP

    fun untilStopped() = WaitUntil(::stopped)

    fun calculateVelocity(): Double {
        val distance = hypot(follower.pose.x - Nav.goal.x, follower.pose.y - Nav.goal.y)
        return 1.158969 + (0.7886366 - 1.158969) / (1 + (distance / 116.6622).pow(2.41902))
    }
}
