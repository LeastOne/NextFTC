package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.FLOAT
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.DigitalChannel.Mode.INPUT
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.ftc.Gamepads.gamepad2
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import org.firstinspires.ftc.threedrd.nextftc.hardware.MotorEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.ServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.device
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.threedrd.util.Debounce
import org.firstinspires.ftc.threedrd.pedropathing.tiles
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.subsystems.Config.alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sign

@Configurable
object Intake : Subsystem() {
    var STOP = 0.0
    var HOLD = 0.50
    var FWD = 1.0
    var REV = -0.25
    var VEL = STOP
    var MAX_ARTIFACTS = 3
    var LASER_THRESHOLD = 0.125
    var BUMPER_LEFT_POS = 0.0
    var BUMPER_LEFT_MIN = 0.5
    var BUMPER_LEFT_MAX = 1.0
    var BUMPER_RIGHT_POS = 1.0
    var BUMPER_RIGHT_MIN = 0.0
    var BUMPER_RIGHT_MAX = 0.5

    val motor = MotorEx("intake") {
        direction = REVERSE
        mode = RUN_USING_ENCODER
        zeroPowerBehavior = FLOAT
    }

    val laser by device(DigitalChannel::class.java, "laser2") { mode = INPUT }
    val bumperLeft = ServoEx("bumperLeft") { scaleRange(BUMPER_LEFT_MIN, BUMPER_LEFT_MAX) }
    val bumperRight = ServoEx("bumperRight") { scaleRange(BUMPER_RIGHT_MIN, BUMPER_RIGHT_MAX) }

    val laserDebounce = Debounce()
    var full = false
    var artifacts = 0

    val forward by instant { VEL = FWD }
    val reverse by instant { VEL = REV }
    val hold by instant { VEL = HOLD }
    val stop by instant { VEL = STOP }
    val reset by instant { artifacts = 0; full = false }

    override fun initialize() {
        VEL = STOP
        artifacts = 0
        full = false
        laserDebounce.reset()
    }

    override fun controls() {
        val intake = !gamepad2.start and gamepad2.a
        (intake and gamepad2.dpadUp) whenBecomesTrue forward
        (intake and gamepad2.dpadDown) whenBecomesTrue reverse
        (intake and (gamepad2.dpadLeft or gamepad2.dpadRight)) whenBecomesTrue stop
    }

    override fun periodic() {
        val bumperPosition = if (
            hypot(follower.pose.x - Nav.gateIntake.x, follower.pose.y - Nav.gateIntake.y) < 1.tiles.inIn
        ) {
            if (alliance == RED) 1.0 else 0.0
        } else {
            1 - max(0.0, follower.pose.y.sign * (abs(follower.pose.y) - abs(follower.pose.x)).sign)
        }

        BUMPER_LEFT_POS = bumperPosition
        BUMPER_RIGHT_POS = bumperPosition

        motor.update { velocityPercentage = VEL }
        bumperLeft.update { position = if (state.started && VEL > HOLD) BUMPER_LEFT_POS else 1.0 }
        bumperRight.update { position = if (state.started && VEL > HOLD) BUMPER_RIGHT_POS else 0.0 }

        val blocked = laser.state
        if (laserDebounce.triggered(blocked, LASER_THRESHOLD) && artifacts < MAX_ARTIFACTS)
            full = ++artifacts >= MAX_ARTIFACTS

        tel.info("Artifacts", artifacts)
        tel.debug("Laser", blocked)
    }

    override fun stop() {
        VEL = STOP
        motor.power = STOP
    }

    fun untilElement() = deferred {
        val target = artifacts + 1
        WaitUntil { artifacts == target || full }
    }

    fun untilFull() = WaitUntil { full }

    fun untilArtifacts(count: Int) = WaitUntil { artifacts >= count }
}
