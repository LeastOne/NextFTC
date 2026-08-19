package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import dev.nextftc.bindings.Range
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.ftc.Gamepads.gamepad1
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.DriveSubsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.PedroDriverControlled
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import kotlin.math.PI

@Configurable
object Drive : DriveSubsystem() {
    var POWER_LOW = 0.5
    var POWER_MEDIUM = 0.75
    var POWER_HIGH = 1.0

    private lateinit var forwardInput: Range
    private lateinit var strafeInput: Range
    private lateinit var turnInput: Range

    val driverControlled = PedroDriverControlled(
        { -forwardInput.get() },
        { -strafeInput.get() },
        { -turnInput.get() }
    ) { config.robotCentric }.apply {
        requires(this@Drive)
    }

    val low by instant { driverControlled.scalar = POWER_LOW }
    val medium by instant { driverControlled.scalar = POWER_MEDIUM }
    val high by instant { driverControlled.scalar = POWER_HIGH }
    val toggleCentric by instant { config.robotCentric = !config.robotCentric }

    override val defaultCommand
        get() = if (state.teleop) driverControlled else super.defaultCommand

    override fun initialize() {
        forwardInput = gamepad1.leftStickY
        strafeInput = gamepad1.leftStickX
        turnInput = gamepad1.rightStickX
        driverControlled.scalar = POWER_HIGH
    }

    override fun controls() {
        val driving = !gamepad1.back
        (driving and gamepad1.dpadDown) whenBecomesTrue low
        (driving and (gamepad1.dpadLeft or gamepad1.dpadRight)) whenBecomesTrue medium
        (driving and gamepad1.dpadUp) whenBecomesTrue high
        (gamepad1.back and gamepad1.start) whenBecomesTrue toggleCentric
    }

    override fun periodic() {
        val pose = follower.pose
        tel.info("Power", "%.2f".format(driverControlled.scalar))
        tel.debug("X", "%.1f".format(pose.x))
        tel.debug("Y", "%.1f".format(pose.y))
        tel.debug("Heading (deg)", "%.1f".format(pose.heading * 180 / PI))
        tel.verbose("Busy", follower.isBusy)
    }
}
