package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ConstantsTests {
    @Test
    fun definesTheRobotDimensions() {
        assertEquals(14.25, Constants.robotLength.inIn, 0.0001)
        assertEquals(11.375, Constants.robotWidth.inIn, 0.0001)
        assertEquals(7.125, Constants.robotRadius, 0.0001)
    }

    @Test
    fun buildsTheFollowerFromPedroConstants() {
        val originalFollower = Constants.followerConstants
        val originalPaths = Constants.pathConstraints
        val originalDrivetrain = Constants.driveConstants
        val originalLocalizer = Constants.localizerConstants
        val followerConstants = mock(FollowerConstants::class.java)
        val paths = mock(PathConstraints::class.java)
        val drivetrain = mock(MecanumConstants::class.java)
        val localizer = mock(PinpointConstants::class.java)
        val hardwareMap = mock(HardwareMap::class.java)
        val follower = mock(Follower::class.java)
        var constructorArguments = emptyList<Any?>()

        try {
            Constants.followerConstants = followerConstants
            Constants.pathConstraints = paths
            Constants.driveConstants = drivetrain
            Constants.localizerConstants = localizer

            mockConstruction(
                FollowerBuilder::class.java,
                { mock, context ->
                    constructorArguments = context.arguments()
                    `when`(mock.pathConstraints(paths)).thenReturn(mock)
                    `when`(mock.mecanumDrivetrain(drivetrain)).thenReturn(mock)
                    `when`(mock.pinpointLocalizer(localizer)).thenReturn(mock)
                    `when`(mock.build()).thenReturn(follower)
                }
            ).use { construction ->
                assertSame(follower, Constants.createFollower(hardwareMap))

                val builder = construction.constructed().single()
                assertSame(followerConstants, constructorArguments[0])
                assertSame(hardwareMap, constructorArguments[1])
                verify(builder).pathConstraints(paths)
                verify(builder).mecanumDrivetrain(drivetrain)
                verify(builder).pinpointLocalizer(localizer)
            }
        } finally {
            Constants.followerConstants = originalFollower
            Constants.pathConstraints = originalPaths
            Constants.driveConstants = originalDrivetrain
            Constants.localizerConstants = originalLocalizer
        }
    }
}
