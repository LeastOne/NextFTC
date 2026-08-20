package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.geometry.Pose
import com.pedropathing.follower.Follower
import dev.nextftc.extensions.pedro.PedroComponent
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
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class NavTests : SubsystemTests() {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }.apply { preInit() }
        `when`(follower.pose).thenReturn(Pose())
        Vision.element = null
        Vision.botpose = null
        Config.config.goalDistanceOffsetNorth = 0.0
        Config.config.goalDistanceOffsetSouth = 0.0
        Config.config.goalAngleOffsetNorth = 0.0
        Config.config.goalAngleOffsetSouth = 0.0
    }

    @After
    fun resetConfig() {
        Config.config.alliance = UNKNOWN
        Config.config.side = UNKNOWN_SIDE
        component.postStop()
    }

    @Test
    fun createsCenteredPosesFromDistances() {
        listOf(Config.config.goalDistanceOffsetNorth, Config.config.goalDistanceOffsetSouth,
            Config.config.goalAngleOffsetNorth, Config.config.goalAngleOffsetSouth)
            .forEach { assertFalse(it.isNaN()) }
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

    @Test
    fun seasonPosesReflectAllianceSideAndApproach() {
        Config.config.alliance = RED
        Config.config.side = NORTH
        `when`(follower.pose).thenReturn(Pose(0.0, 0.0))
        listOf(Nav.spike0, Nav.spike1, Nav.spike2, Nav.spike3, Nav.gate,
            Nav.gateIntake, Nav.gateIntakeDepart, Nav.goal, Nav.chaseScan, Nav.base)
            .forEach { assertFalse(it.x.isNaN()) }
        assertFalse(Nav.depositSouth().heading.isNaN())
        assertFalse(Nav.depositNorth().heading.isNaN())

        Config.config.alliance = BLUE
        Config.config.side = SOUTH
        assertFalse(Nav.gate.x.isNaN())
        `when`(follower.pose).thenReturn(Pose(-60.0, 0.0))
        assertFalse(Nav.spike0.heading.isNaN())
        assertFalse(Nav.depositSouth(1.inches, 2.inches).heading.isNaN())
        assertFalse(Nav.depositNorth().heading.isNaN())

        `when`(follower.pose).thenReturn(Pose(60.0, 0.0))
        assertFalse(Nav.depositSouth().heading.isNaN())
    }

    @Test
    fun artifactsUseVisionOrAStableBackup() {
        Config.config.alliance = RED
        `when`(follower.pose).thenReturn(Pose(50.0, -10.0, 0.2))
        assertEquals(Nav.backupArtifact.x, Nav.artifact.x, 0.0)
        assertEquals(Nav.backupArtifact.y, Nav.artifact.y, 0.0)
        assertFalse(Nav.chase(2).x.isNaN())

        Vision.element = Pose(60.0, -65.0, -PI / 2)
        assertEquals(60.0, Nav.chase(2).x, 0.0)
        assertEquals(60.0, Nav.artifact.x, 0.0)
        assertFalse(Nav.artifactForwardRemaining.isNaN())
        assertFalse(Nav.artifactStrafeRemaining.isNaN())
        assertFalse(Nav.artifactHeadingRemaining.isNaN())

        `when`(follower.pose).thenReturn(Nav.artifact)
        assertEquals(Nav.artifact.y, Nav.artifactApproachY(Nav.artifact), 0.0)
        `when`(follower.pose).thenReturn(Pose(0.0, 0.0))
        assertEquals(Nav.robotLength.inIn / 2, Nav.artifactApproachY(Nav.artifact), 0.0)
        `when`(follower.pose).thenReturn(Pose(0.0, 70.0))
        assertTrue(Nav.artifactApproachY(Nav.artifact) < 70.0)
        `when`(follower.pose).thenReturn(Pose(0.0, 30.0))
        assertEquals(30.0, Nav.artifactApproachY(Nav.artifact), 0.0)
        assertEquals(-70.0, Nav.allianceSideY(-70.0), 0.0)
    }

    @Test
    fun parkingAndGoalTargetingCoverFieldVariations() {
        Config.config.alliance = RED
        Config.config.side = NORTH
        assertFalse(Nav.parking(true).x.isNaN())
        assertFalse(Nav.parking(false, FRONT, LEFT).x.isNaN())

        Config.config.alliance = BLUE
        Config.config.side = SOUTH
        assertFalse(Nav.parking(false, BACK, RIGHT).x.isNaN())

        Config.config.goalDistanceOffsetNorth = 1.0
        Config.config.goalDistanceOffsetSouth = 2.0
        Config.config.goalAngleOffsetNorth = 3.0
        Config.config.goalAngleOffsetSouth = 4.0
        `when`(follower.pose).thenReturn(Pose(60.0, 20.0, 0.5))
        Vision.botpose = Pose(59.0, 19.0, 0.5)
        assertEquals(1.0, Nav.goalDistanceOffset, 0.0)
        assertFalse(Nav.goalDistance.isNaN())
        assertFalse(Nav.goalHeadingOffset.isNaN())
        assertFalse(Nav.goalHeadingRemaining.isNaN())

        `when`(follower.pose).thenReturn(Pose(-60.0, 20.0, 0.5))
        Vision.botpose = null
        assertEquals(2.0, Nav.goalDistanceOffset, 0.0)
        assertFalse(Nav.goalDistance.isNaN())
        assertFalse(Nav.goalHeadingRemaining.isNaN())
        assertTrue(Nav.goalHeadingOffset != 0.0)
    }
}
