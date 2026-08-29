package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.geometry.Pose
import dev.nextftc.core.units.Distance
import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import kotlin.math.abs
import kotlin.math.atan2
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial.CENTER
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral.CENTER as LATERAL_CENTER
import org.firstinspires.ftc.threedrd.nextftc.subsystems.NavSubsystem
import org.firstinspires.ftc.threedrd.pedropathing.axial
import org.firstinspires.ftc.threedrd.pedropathing.face
import org.firstinspires.ftc.threedrd.pedropathing.lateral
import org.firstinspires.ftc.threedrd.pedropathing.normalizeHeading
import org.firstinspires.ftc.threedrd.pedropathing.tiles
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.side

@Configurable
object Nav : NavSubsystem(Constants.robotLength, Constants.robotWidth) {
    val start get() = when {
        alliance == UNKNOWN || side == UNKNOWN_SIDE -> pose(0.tiles, 0.tiles)
        side == NORTH -> pose(2.7.tiles, alliance(-0.8).tiles)
        else -> pose(-2.7.tiles, alliance(-0.8).tiles)
    }

    val spike0 get() = pose(2.1.tiles, alliance(if (alliance == RED) -2.75 else -2.65).tiles, alliance(-15).deg)
    val spike1 get() = pose(1.5.tiles, alliance(-1.1).tiles, alliance(-90).deg)
    val spike2 get() = pose(0.5.tiles, alliance(-1.1).tiles, alliance(-90).deg)
    val spike3 get() = pose(-0.55.tiles, alliance(-1.1).tiles, alliance(-90).deg)
    val score = pose(1.tiles, 0.5.tiles, 90.deg)
    val park = pose(1.5.tiles, 0.tiles)
    val gate get() = pose((if (side == NORTH) 0.15 else -0.15).tiles, alliance(-2).tiles, alliance(-90).deg)
    val gateIntake get() = pose(0.65.tiles, alliance(-2.65).tiles, alliance(-135).deg)
    val gateIntakeDepart get() = pose(0.25.tiles, alliance(-2.65).tiles, alliance(220).deg)
    val goal get() = pose(-2.75.tiles, alliance(-2.75).tiles, alliance(45).deg)
    val chaseScan get() = pose(2.25.tiles, alliance(-0.75).tiles, alliance(-85).deg)
    val base get() = pose(1.5.tiles, alliance(1.33).tiles)

    fun depositSouth(
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ): Pose {
        val offset = alliance(when {
            follower.pose.x < -2.tiles.inIn -> -182.0
            follower.pose.x > 2.tiles.inIn -> -170.0
            else -> -175.0
        })
        return pose(-1.tiles, alliance(-0.75).tiles)
            .face(goal, offset)
            .axial(axialOffset)
            .lateral(lateralOffset)
            .face(goal, offset)
    }

    fun depositNorth(
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ): Pose {
        val offset = alliance(if (alliance == RED) -177.0 else 178.0)
        return pose(2.3.tiles, alliance(-0.6).tiles)
            .face(goal, offset)
            .axial(axialOffset)
            .lateral(lateralOffset)
            .face(goal, offset)
    }

    fun chase(execution: Int) = pose(
        (Vision.element?.x ?: ((2.75 - execution % 3 * 0.75).tiles.inIn)).inches,
        alliance(-2.4).tiles,
        alliance(-90).deg
    )

    val artifact: Pose
        get() {
            val element = Vision.element ?: return backupArtifact
            return Pose(
                element.x.coerceIn(2.tiles.inIn + 2 + robotWidth.inIn / 2, 3.tiles.inIn - robotWidth.inIn / 2),
                alliance(Vision.ELEMENT_RADIUS - 3.tiles.inIn),
                alliance(-90).deg.inRad
            )
        }

    val backupArtifact get() = Pose(
        3.tiles.inIn - Vision.ELEMENT_RADIUS * 5,
        alliance(Vision.ELEMENT_RADIUS - 3.tiles.inIn)
    )

    val artifactForwardRemaining get() = follower.pose.x - artifact.x
    val artifactStrafeRemaining get() = follower.pose.y - artifactApproachY(artifact)
    val artifactHeadingRemaining get() = (follower.pose.heading - artifact.heading).normalizeHeading()

    fun artifactApproachY(artifact: Pose): Double {
        if (abs(artifactForwardRemaining) <= Vision.ELEMENT_RADIUS) return artifact.y
        val staging = artifact.y + alliance(Vision.ELEMENT_RADIUS * 3)
        if (allianceSideY(follower.pose.y) < robotLength.inIn / 2)
            return alliance(-robotLength.inIn / 2)
        return if (allianceSideY(follower.pose.y) > allianceSideY(staging)) staging else follower.pose.y
    }

    fun allianceSideY(y: Double) = alliance(-y)

    fun parking(gate: Boolean, axial: Axial = CENTER, lateral: Lateral = LATERAL_CENTER) = pose(
        (if (gate) 0.0 else side(if (side == NORTH) 2.6 else 2.4)).tiles,
        alliance(if (gate) -1.75 else if (side == NORTH) -1.75 else -1.0).tiles,
        (if (gate) alliance(-90) else 90 + side(90)).deg,
        axial,
        lateral
    )

    val goalDistanceOffset get() =
        if (follower.pose.x > 1.tiles.inIn) config.goalDistanceOffsetNorth
        else config.goalDistanceOffsetSouth

    val goalDistance get() = (
        Vision.botpose ?: follower.pose
    ).distanceFrom(goal) + goalDistanceOffset

    val goalHeadingOffset get() = (
        if (follower.pose.x > 1.tiles.inIn) config.goalAngleOffsetNorth
        else config.goalAngleOffsetSouth
    ).deg.inRad

    val goalHeadingRemaining get() = run {
        val pose = Vision.botpose ?: follower.pose
        val heading = atan2(goal.y - pose.y, goal.x - pose.x) + goalHeadingOffset
        (follower.pose.heading - heading).normalizeHeading()
    }
}
