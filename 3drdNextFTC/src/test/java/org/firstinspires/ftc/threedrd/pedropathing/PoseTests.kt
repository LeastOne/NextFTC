package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.geometry.Pose
import dev.nextftc.core.units.inches
import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseTests {
    @Test
    fun translatesRelativeToHeading() {
        val pose = Pose(10.0, 20.0, PI / 2)

        val axial = pose.axial(5.0)
        val lateral = pose.lateral(5.0)
        val measuredAxial = pose.axial(5.inches)
        val measuredLateral = pose.lateral(5.inches)

        assertEquals(10.0, axial.x, 0.0001)
        assertEquals(25.0, axial.y, 0.0001)
        assertEquals(5.0, lateral.x, 0.0001)
        assertEquals(20.0, lateral.y, 0.0001)
        assertEquals(axial.x, measuredAxial.x, 0.0)
        assertEquals(axial.y, measuredAxial.y, 0.0)
        assertEquals(lateral.x, measuredLateral.x, 0.0)
        assertEquals(lateral.y, measuredLateral.y, 0.0)
    }

    @Test
    fun transformsHeadingAndFacesPoses() {
        val pose = Pose(1.0, 2.0, Math.toRadians(170.0))

        assertEquals(Math.toRadians(-10.0), pose.reverse().heading, 0.0001)
        assertEquals(0.0, pose.face(Pose(2.0, 2.0)).heading, 0.0001)
        assertEquals(PI / 2, pose.face(Pose(2.0, 2.0), 90.0).heading, 0.0001)
        assertEquals(Math.toRadians(-170.0), pose.turn(20.0).heading, 0.0001)
    }

    @Test
    fun midpointUsesTheShortestHeadingDifference() {
        val midpoint = Pose(0.0, 0.0, Math.toRadians(170.0))
            .midpoint(Pose(2.0, 4.0, Math.toRadians(-170.0)))

        assertEquals(1.0, midpoint.x, 0.0)
        assertEquals(2.0, midpoint.y, 0.0)
        assertEquals(PI, midpoint.heading, 0.0001)
    }

    @Test
    fun normalizesAnyNumberOfRevolutions() {
        assertEquals(PI, (5 * PI).normalizeHeading(), 0.0)
        assertEquals(-PI, (-5 * PI).normalizeHeading(), 0.0)
    }
}
