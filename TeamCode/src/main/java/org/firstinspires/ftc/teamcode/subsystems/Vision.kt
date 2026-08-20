package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.geometry.Pose
import com.qualcomm.hardware.limelightvision.LLResult
import com.qualcomm.hardware.limelightvision.Limelight3A
import com.qualcomm.robotcore.util.ElapsedTime
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.tan
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.ServoEx
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.device
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.update
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.axial
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.lateral
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.adaptations.quanomous.Quanomous
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.APRIL_TAG
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.GREEN
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.PURPLE
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.QR_CODE
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state

@Configurable
object Vision : Subsystem() {
    var CAMERA_UPSIDE_DOWN = true
    var CAMERA_X_INCHES = 3.93701
    var CAMERA_Y_INCHES = -0.3937008
    var CAMERA_Z_INCHES = 16.14173
    var CAMERA_PITCH_DEGREES = -0.75
    var CAMERA_YAW_DEGREES = 1.15
    var ELEMENT_RADIUS = 2.5
    var ELEVATION_SCALAR = 1.0
    var BEARING_X_SCALAR = 1.0
    var BEARING_Y_SCALAR = 1.0
    var POS_GOAL_LOCK = 0.10
    var POS_CHASE_LOCK = 0.83
    var POS_MIN = 0.10
    var POS_MAX = 0.85
    var POS = 1.0
    var SETTLE_SECONDS = 0.6

    val limelightDevice = device(Limelight3A::class.java, "limelight") { pipelineSwitch(QR_CODE.index)
        start()
    }
    val limelight by limelightDevice
    val servo = ServoEx("turret") { scaleRange(POS_MIN, POS_MAX) }

    var pipeline = QR_CODE
    var timer = ElapsedTime()
    var result: LLResult? = null
    var botpose: Pose? = null
    var element: Pose? = null
    val purpleArtifacts = mutableListOf<Pose>()
    val greenArtifacts = mutableListOf<Pose>()

    val goalLock by instant { goalLock(true) }
    val goalUnlock by instant { goalLock(false) }
    val chaseLock by instant { chaseLock(true) }
    val chaseUnlock by instant { chaseLock(false) }
    val reset by instant { resetElement() }
    val backup by instant { setBackupElement() }

    override fun initialize() {
        pipeline = QR_CODE
        POS = 1.0
        result = null
        botpose = null
        element = null
        purpleArtifacts.clear()
        greenArtifacts.clear()
        timer.reset()
    }

    override fun periodic() {
        if (!limelight.isConnected) {
            tel.error("Status", "Connection issue")
            return
        }

        limelight.updateRobotOrientation(Math.toDegrees(follower.pose.heading))
        result = limelight.latestResult
        botpose = null
        servo.update { position = POS }

        val current = result
        if (current != null && current.isValid && timer.seconds() >= SETTLE_SECONDS) {
            when (pipeline) {
                QR_CODE -> processQrCode(current)
                APRIL_TAG -> processAprilTag(current)
                GREEN -> processColor(current, greenArtifacts, purpleArtifacts)
                PURPLE -> processColor(current, purpleArtifacts, greenArtifacts)
            }
        }

        tel.info("Pipeline", pipeline)
        tel.debug("Results", current?.isValid == true)
        botpose?.let { tel.debug("Botpose", "%.1f, %.1f, %.1f°".format(it.x, it.y, Math.toDegrees(it.heading))) }
        element?.let { tel.debug("Element", "%.1f, %.1f".format(it.x, it.y)) }
    }

    override fun stop() {
        limelight.stop()
    }

    fun goalLock(enabled: Boolean) {
        if (!enabled || !state.teleop) return
        switchPipeline(APRIL_TAG)
        POS = POS_GOAL_LOCK
    }

    fun chaseLock(enabled: Boolean) {
        if (!enabled) return
        switchPipeline(PURPLE, true)
        POS = POS_CHASE_LOCK
    }

    fun switchPipeline(pipeline: Pipeline, resetElement: Boolean = false) {
        if (resetElement) resetElement()
        this.pipeline = pipeline
        limelight.pipelineSwitch(pipeline.index)
        if (pipeline == GREEN || pipeline == PURPLE) POS = POS_CHASE_LOCK
        timer.reset()
    }

    fun resetElement() {
        val selected = element ?: return
        purpleArtifacts.removeAll { distance(it, selected) <= ELEMENT_RADIUS }
        greenArtifacts.removeAll { distance(it, selected) <= ELEMENT_RADIUS }
        element = null
    }

    fun setBackupElement() {
        if (element == null) element = Pose(2.5.tiles.inIn, config.alliance.sign * -2.9.tiles.inIn)
    }

    fun waitForElement() = WaitUntil { element != null }

    fun processQrCode(result: LLResult) {
        result.barcodeResults.forEach { barcode ->
            config.quanomous = Quanomous.process(barcode.data)
            ConfigComponent.changed()
            log.info("QR Code | ${barcode.family} | ${barcode.data}")
        }
    }

    fun processAprilTag(result: LLResult) {
        val fiducial = result.fiducialResults.firstOrNull() ?: return
        if ((config.alliance == BLUE && fiducial.fiducialId != 20) ||
            (config.alliance == RED && fiducial.fiducialId != 24)) return

        val pose = result.botpose_MT2
        botpose = Pose(
            pose.position.x * INCHES_PER_METER,
            pose.position.y * INCHES_PER_METER,
            pose.orientation.getYaw(RADIANS)
        )
    }

    fun processColor(result: LLResult, primary: MutableList<Pose>, secondary: List<Pose>) {
        if (!state.started) return
        result.colorResults.forEach { color ->
            val direction = if (CAMERA_UPSIDE_DOWN) -1 else 1
            val pose = elementPose(
                direction * color.targetXDegrees,
                direction * color.targetYDegrees
            )
            primary.removeAll { distance(it, pose) <= ELEMENT_RADIUS }
            primary += pose
        }
        element = (primary + secondary).minByOrNull { distance(follower.pose, it) } ?: element
    }

    fun elementPose(targetYawDegrees: Double, targetPitchDegrees: Double): Pose {
        val height = CAMERA_Z_INCHES - ELEMENT_RADIUS / 2
        val elevation = Math.toRadians(CAMERA_PITCH_DEGREES + targetPitchDegrees)
        val bearing = Math.toRadians(CAMERA_YAW_DEGREES - targetYawDegrees)
        val distance = abs(height / tan(elevation * ELEVATION_SCALAR))
        val x = CAMERA_X_INCHES + distance * cos(bearing * BEARING_X_SCALAR)
        val y = CAMERA_Y_INCHES + distance * sin(bearing * BEARING_Y_SCALAR)
        return follower.pose.axial(x).lateral(y).withHeading(atan2(y, x))
    }

    fun distance(first: Pose, second: Pose) = hypot(first.x - second.x, first.y - second.y)

    const val INCHES_PER_METER = 39.3701
}
