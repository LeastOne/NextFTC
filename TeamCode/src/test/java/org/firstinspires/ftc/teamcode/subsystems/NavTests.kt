package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.geometry.Pose
import dev.nextftc.core.units.inches
import kotlin.math.PI
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.BACK
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial.FRONT
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.LEFT
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral.RIGHT
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.TILE_WIDTH
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tile
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.SOUTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Test

class NavTests {
    @After
    fun resetConfig() {
        Config.config.alliance = UNKNOWN
        Config.config.side = UNKNOWN_SIDE
    }

    @Test
    fun createsCenteredPosesFromDistances() {
        assertEquals(14.25, Nav.robotLength.inIn, 0.0001)
        assertEquals(11.375, Nav.robotWidth.inIn, 0.0001)
        val centered = Nav.pose(1.inches, 2.inches)
        assertEquals(1.0, centered.x, 0.0)
        assertEquals(2.0, centered.y, 0.0)
        assertEquals(0.0, centered.heading, 0.0)
    }

    @Test
    fun providesSampleAutonomousPoses() {
        Config.config.alliance = UNKNOWN
        Config.config.side = UNKNOWN_SIDE
        assertEquals(0.0, Nav.start.x, 0.0)
        assertEquals(0.0, Nav.start.y, 0.0)
        assertEquals(23.5, Nav.score.x, 0.0)
        assertEquals(11.75, Nav.score.y, 0.0)
        assertEquals(PI / 2, Nav.score.heading, 0.0)
        assertEquals(35.25, Nav.park.x, 0.0)
        assertEquals(0.0, Nav.park.y, 0.0)
    }

    @Test
    fun startPoseReflectsAllianceAndSide() {
        Config.config.alliance = RED
        Config.config.side = NORTH
        assertEquals(63.45, Nav.start.x, 0.0001)
        assertEquals(18.8, Nav.start.y, 0.0001)
        assertEquals(0.0, Nav.start.heading, 0.0)

        Config.config.side = SOUTH
        assertEquals(-63.45, Nav.start.x, 0.0001)
        assertEquals(18.8, Nav.start.y, 0.0001)
        assertEquals(0.0, Nav.start.heading, 0.0)

        Config.config.alliance = BLUE
        Config.config.side = NORTH
        assertEquals(63.45, Nav.start.x, 0.0001)
        assertEquals(-18.8, Nav.start.y, 0.0001)
        assertEquals(0.0, Nav.start.heading, 0.0)

        Config.config.side = SOUTH
        assertEquals(-63.45, Nav.start.x, 0.0001)
        assertEquals(-18.8, Nav.start.y, 0.0001)
        assertEquals(0.0, Nav.start.heading, 0.0)

        Config.config.side = UNKNOWN_SIDE
        assertEquals(0.0, Nav.start.x, 0.0)
        assertEquals(0.0, Nav.start.y, 0.0)
    }

    @Test
    fun tilesAreDistancesBasedOnFieldTileWidth() {
        assertEquals(23.5, TILE_WIDTH.inIn, 0.0)
        assertEquals(23.5, 1.tile.inIn, 0.0)
        assertEquals(47.0, 2.tiles.inIn, 0.0)
        assertEquals(11.75, 0.5.tiles.inIn, 0.0)
    }

    @Test
    fun poseCanOffsetFromRobotEdges() {
        val pose = Nav.pose(
            20.inches, 30.inches, PI / 2,
            axial = FRONT,
            lateral = LEFT,
            axialOffset = 2.inches,
            lateralOffset = 1.inches
        )

        assertEquals(24.6875, pose.x, 0.0001)
        assertEquals(24.875, pose.y, 0.0001)
        assertEquals(PI / 2, pose.heading, 0.0)
    }

    @Test
    fun poseCanOffsetFromBackAndRightEdges() {
        val pose = Nav.pose(
            20.inches, 30.inches,
            axial = BACK,
            lateral = RIGHT
        )

        assertEquals(27.125, pose.x, 0.0001)
        assertEquals(35.6875, pose.y, 0.0001)
    }

    @Test
    fun lineConnectsPosesAndInterpolatesHeading() {
        val start = Pose(1.0, 2.0, 0.25)
        val end = Pose(3.0, 4.0, 1.25)

        val path = Nav.line(start, end)

        assertEquals(start, path.firstControlPoint)
        assertEquals(end, path.lastControlPoint)
        assertEquals(start.heading, path.getHeadingGoal(0.0), 0.0)
        assertEquals(end.heading, path.getHeadingGoal(1.0), 0.0)
    }
}
