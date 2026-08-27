package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.control.FilteredPIDFCoefficients
import com.pedropathing.control.PIDFCoefficients
import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver.EncoderDirection.FORWARD
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.units.inches
import kotlin.math.max

/**
 * Neutral Pedro configuration scaffold for a new robot.
 *
 * Replace these placeholder dimensions, hardware names, localizer geometry, and tuned values
 * in the season-specific robot implementation while preserving Pedro's documented structure.
 */
object Constants {
    val robotLength = 1.0.inches
    val robotWidth = 1.0.inches
    val robotRadius = max(robotLength.inIn, robotWidth.inIn) / 2

    var followerConstants = FollowerConstants()
        .mass(1.0)
        .forwardZeroPowerAcceleration(-1.0)
        .lateralZeroPowerAcceleration(-1.0)
        .translationalPIDFCoefficients(PIDFCoefficients(0.0, 0.0, 0.0, 0.0))
        .headingPIDFCoefficients(PIDFCoefficients(0.0, 0.0, 0.0, 0.0))
        .drivePIDFCoefficients(FilteredPIDFCoefficients(0.0, 0.0, 0.0, 0.0, 0.0))

    var pathConstraints = PathConstraints(0.995, 100.0, 0.9, 1.0)

    var driveConstants = MecanumConstants()
        .maxPower(1.0)
        .xVelocity(1.0)
        .yVelocity(1.0)
        .leftFrontMotorName("frontLeft")
        .rightFrontMotorName("frontRight")
        .leftRearMotorName("backLeft")
        .rightRearMotorName("backRight")

    var localizerConstants = PinpointConstants()
        .forwardPodY(0.0)
        .strafePodX(0.0)
        .forwardEncoderDirection(FORWARD)
        .strafeEncoderDirection(FORWARD)

    fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .pinpointLocalizer(localizerConstants)
            .build()
}
