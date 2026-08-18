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

object Constants {
    var followerConstants = FollowerConstants()
        .mass(12.5628)
        .forwardZeroPowerAcceleration(-24.591773413810188)
        .lateralZeroPowerAcceleration(-76.0984478775747)
        .translationalPIDFCoefficients(PIDFCoefficients(0.1, 0.0, 0.0, 0.015))
        .headingPIDFCoefficients(PIDFCoefficients(0.75, 0.0, 0.0, 0.0075))
        .drivePIDFCoefficients(FilteredPIDFCoefficients(0.02125, 0.0, 0.0000085, 0.51, 0.0085))

    var pathConstraints = PathConstraints(0.995, 100.0, 0.9, 1.0)

    var driveConstants = MecanumConstants()
        .maxPower(1.0)
        .xVelocity(73.62513937161664)
        .yVelocity(56.98721866157111)
        .leftFrontMotorName("driveFrontLeft")
        .rightFrontMotorName("driveFrontRight")
        .leftRearMotorName("driveBackLeft")
        .rightRearMotorName("driveBackRight")

    var localizerConstants = PinpointConstants()
        .forwardPodY(4.7244)
        .strafePodX(1.996)
        .forwardEncoderDirection(FORWARD)
        .strafeEncoderDirection(FORWARD)

    fun createFollower(hardwareMap: HardwareMap): Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .pinpointLocalizer(localizerConstants)
            .build()
}
