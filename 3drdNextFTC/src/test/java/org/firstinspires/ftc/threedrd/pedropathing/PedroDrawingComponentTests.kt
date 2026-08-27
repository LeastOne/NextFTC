package org.firstinspires.ftc.threedrd.pedropathing

import com.bylazar.field.Circle
import com.bylazar.field.FieldManager
import com.bylazar.field.Line
import com.bylazar.field.PanelsField
import com.bylazar.field.Style
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.Path
import com.pedropathing.util.PoseHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PedroDrawingComponentTests {
    lateinit var field: FieldManager
    lateinit var follower: Follower
    lateinit var history: PoseHistory
    lateinit var drawing: PedroDrawingComponent

    @Before
    fun setUp() {
        field = FieldManager()
        follower = mock(Follower::class.java)
        history = mock(PoseHistory::class.java)
        drawing = PedroDrawingComponent(field, { follower }, 7.125)

        `when`(history.xPositionsArray).thenReturn(doubleArrayOf(1.0, 2.0))
        `when`(history.yPositionsArray).thenReturn(doubleArrayOf(3.0, 4.0))
        `when`(follower.poseHistory).thenReturn(history)
        `when`(follower.pose).thenReturn(Pose(5.0, 6.0, 0.0))
    }

    @Test
    fun initializesThePedroFieldAndDrawsDuringInit() {
        drawing.preInit()
        field.lastUpdate = 0

        drawing.postWaitForStart()

        val preset = field.lastCanvas.preset
        assertEquals(PanelsField.presets.DEFAULT_FTC, preset)
        assertEquals(Style("#666", "#3F51B5", 2.0), drawing.targetStyle)
        assertEquals(Style("#666", "#4CAF50", 2.0), drawing.robotStyle)
        assertEquals(7.125, drawing.robotRadius, 0.0)
        assertEquals(3, field.lastCanvas.items.size)
        assertTrue(field.lastCanvas.items[0] is Line)
        assertTrue(field.lastCanvas.items[1] is Circle)
        assertTrue(field.lastCanvas.items[2] is Line)
    }

    @Test
    fun drawsTheActivePathTargetHistoryAndRobotDuringUpdate() {
        val path = mock(Path::class.java)
        `when`(path.panelsDrawingPoints).thenReturn(arrayOf(
            doubleArrayOf(10.0, 20.0, 30.0),
            doubleArrayOf(40.0, 50.0, 60.0)
        ))
        `when`(path.closestPointTValue).thenReturn(0.5)
        `when`(path.getHeadingGoal(0.5)).thenReturn(Math.PI / 2)
        `when`(follower.currentPath).thenReturn(path)
        `when`(follower.getPointFromPath(0.5)).thenReturn(Pose(20.0, 50.0))
        field.lastUpdate = 0

        drawing.postUpdate()

        val items = field.lastCanvas.items
        assertEquals(7, items.size)
        assertEquals(drawing.targetStyle, (items[0] as Line).style)
        assertEquals(drawing.targetStyle, (items[2] as Circle).style)
        assertEquals(drawing.robotStyle, (items[4] as Line).style)
        assertEquals(drawing.robotStyle, (items[5] as Circle).style)
        assertEquals(drawing.robotRadius, (items[5] as Circle).r, 0.0)
    }
}
