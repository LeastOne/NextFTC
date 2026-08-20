package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.geometry.Pose
import dev.nextftc.core.units.Distance
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.CENTER
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.CENTER as LATERAL_CENTER
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.NavSubsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.axial
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.face
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.lateral
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.normalizeHeading
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.config

@Configurable
object Nav : NavSubsystem() {
    override val robotLength = 14.25.inches
    override val robotWidth = 11.375.inches

    val start get() = when {
        config.alliance == UNKNOWN || config.side == UNKNOWN_SIDE -> pose(0.tiles, 0.tiles)
        config.side == NORTH -> pose(2.7.tiles, (config.alliance.sign * -0.8).tiles)
        else -> pose((-2.7).tiles, (config.alliance.sign * -0.8).tiles)
    }

    val spike0 get() = pose(
        2.1.tiles,
        (config.alliance.sign * if (config.alliance == RED) -2.75 else -2.65).tiles,
        Math.toRadians(config.alliance.sign * -15)
    )
    val spike1 get() = pose(1.5.tiles, (config.alliance.sign * -1.1).tiles,
        Math.toRadians(config.alliance.sign * -90))
    val spike2 get() = pose(0.5.tiles, (config.alliance.sign * -1.1).tiles,
        Math.toRadians(config.alliance.sign * -90))
    val spike3 get() = pose((-0.55).tiles, (config.alliance.sign * -1.1).tiles,
        Math.toRadians(config.alliance.sign * -90))

    val score = pose(1.tiles, 0.5.tiles, PI / 2)
    val park = pose(1.5.tiles, 0.tiles)
    val gate get() = pose(
        (if (config.side == NORTH) 0.15 else -0.15).tiles,
        (config.alliance.sign * -2).tiles,
        Math.toRadians(config.alliance.sign * -90)
    )
    val gateIntake get() = pose(0.65.tiles, (config.alliance.sign * -2.65).tiles,
        Math.toRadians(config.alliance.sign * -135))
    val gateIntakeDepart get() = pose(0.25.tiles, (config.alliance.sign * -2.65).tiles,
        Math.toRadians(config.alliance.sign * 220))
    val goal get() = pose((-2.75).tiles, (config.alliance.sign * -2.75).tiles,
        Math.toRadians(config.alliance.sign * 45))
    val chaseScan get() = pose(2.25.tiles, (config.alliance.sign * -0.75).tiles,
        Math.toRadians(config.alliance.sign * -85))
    val base get() = pose(1.5.tiles, (config.alliance.sign * 1.33).tiles)

    fun depositSouth(
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ): Pose {
        val offset = config.alliance.sign * when {
            follower.pose.x < -2.tiles.inIn -> -182.0
            follower.pose.x > 2.tiles.inIn -> -170.0
            else -> -175.0
        }
        return pose((-1).tiles, (config.alliance.sign * -0.75).tiles)
            .face(goal, offset)
            .axial(axialOffset)
            .lateral(lateralOffset)
            .face(goal, offset)
    }

    fun depositNorth(
        axialOffset: Distance = 0.inches,
        lateralOffset: Distance = 0.inches
    ): Pose {
        val offset = config.alliance.sign * if (config.alliance == RED) -177.0 else 178.0
        return pose(2.3.tiles, (config.alliance.sign * -0.6).tiles)
            .face(goal, offset)
            .axial(axialOffset)
            .lateral(lateralOffset)
            .face(goal, offset)
    }

    fun chase(execution: Int) = pose(
        (Vision.element?.x ?: ((2.75 - execution % 3 * 0.75).tiles.inIn)).inches,
        (config.alliance.sign * -2.4).tiles,
        Math.toRadians(config.alliance.sign * -90)
    )

    val artifact: Pose
        get() {
            val element = Vision.element ?: return backupArtifact
            return Pose(
                element.x.coerceIn(2.tiles.inIn + 2 + robotWidth.inIn / 2,
                    3.tiles.inIn - robotWidth.inIn / 2),
                (3.tiles.inIn - Vision.ELEMENT_RADIUS) * -config.alliance.sign,
                -PI / 2 * config.alliance.sign
            )
        }

    val backupArtifact get() = Pose(
        3.tiles.inIn - Vision.ELEMENT_RADIUS * 5,
        (3.tiles.inIn - Vision.ELEMENT_RADIUS) * -config.alliance.sign
    )

    val artifactForwardRemaining get() = follower.pose.x - artifact.x
    val artifactStrafeRemaining get() = follower.pose.y - artifactApproachY(artifact)
    val artifactHeadingRemaining get() = (follower.pose.heading - artifact.heading).normalizeHeading()

    fun artifactApproachY(artifact: Pose): Double {
        if (abs(artifactForwardRemaining) <= Vision.ELEMENT_RADIUS) return artifact.y
        val staging = artifact.y + Vision.ELEMENT_RADIUS * 3 * config.alliance.sign
        if (allianceSideY(follower.pose.y) < robotLength.inIn / 2)
            return robotLength.inIn / 2 * -config.alliance.sign
        return if (allianceSideY(follower.pose.y) > allianceSideY(staging)) staging else follower.pose.y
    }

    fun allianceSideY(y: Double) = y * -config.alliance.sign

    fun parking(
        gate: Boolean,
        axial: Axial = CENTER,
        lateral: Lateral = LATERAL_CENTER
    ) = pose(
        (if (gate) 0.0 else config.side.sign * if (config.side == NORTH) 2.6 else 2.4).tiles,
        (if (gate) -1.75 * config.alliance.sign else
            (if (config.side == NORTH) -1.75 else -1.0) * config.alliance.sign).tiles,
        if (gate) Math.toRadians(config.alliance.sign * -90)
        else Math.toRadians(90 + config.side.sign * 90),
        axial,
        lateral
    )

    val goalDistanceOffset get() = if (follower.pose.x > 1.tiles.inIn)
        config.goalDistanceOffsetNorth else config.goalDistanceOffsetSouth
    val goalDistance get() = (Vision.botpose ?: follower.pose).distanceFrom(goal) + goalDistanceOffset
    val goalHeadingOffset get() = Math.toRadians(if (follower.pose.x > 1.tiles.inIn)
        config.goalAngleOffsetNorth else config.goalAngleOffsetSouth)
    val goalHeadingRemaining get() = (
        follower.pose.heading - (atan2(goal.y - (Vision.botpose ?: follower.pose).y,
            goal.x - (Vision.botpose ?: follower.pose).x) + goalHeadingOffset)
    ).normalizeHeading()
}
