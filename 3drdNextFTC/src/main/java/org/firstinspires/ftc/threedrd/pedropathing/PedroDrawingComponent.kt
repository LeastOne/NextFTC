package org.firstinspires.ftc.threedrd.pedropathing

import com.bylazar.field.FieldManager
import com.bylazar.field.PanelsField
import com.bylazar.field.Style
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.Path
import com.pedropathing.util.PoseHistory
import dev.nextftc.core.components.Component
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower

class PedroDrawingComponent(
    val field: FieldManager = PanelsField.field,
    val getFollower: () -> Follower = { follower },
    val robotRadius: Double = 9.0
) : Component {
    val targetStyle = Style("#666", "#3F51B5", 2.0)
    val robotStyle = Style("#666", "#4CAF50", 2.0)

    override fun preInit() = field.setOffsets(PanelsField.presets.DEFAULT_FTC)
    override fun postWaitForStart() = draw()
    override fun postUpdate() = draw()

    fun draw() {
        val follower = getFollower()
        follower.currentPath?.let { path ->
            draw(path, targetStyle)
            val t = path.closestPointTValue
            val point = follower.getPointFromPath(t)
            draw(Pose(point.x, point.y, path.getHeadingGoal(t)), targetStyle)
        }
        draw(follower.poseHistory, robotStyle)
        draw(follower.pose, robotStyle)
        field.update()
    }

    fun draw(path: Path, style: Style) {
        val points = path.panelsDrawingPoints
        field.setStyle(style)
        field.moveCursor(points[0][0], points[1][0])
        for (i in 1 until points[0].size) field.line(points[0][i], points[1][i])
    }

    fun draw(history: PoseHistory, style: Style) {
        val x = history.xPositionsArray
        val y = history.yPositionsArray
        field.setStyle(style)
        for (i in 1 until x.size) {
            field.moveCursor(x[i - 1], y[i - 1])
            field.line(x[i], y[i])
        }
    }

    fun draw(pose: Pose, style: Style) {
        field.setStyle(style)
        field.moveCursor(pose.x, pose.y)
        field.circle(robotRadius)

        val heading = pose.headingAsUnitVector
        heading.magnitude *= robotRadius
        field.moveCursor(pose.x + heading.xComponent / 2, pose.y + heading.yComponent / 2)
        field.line(pose.x + heading.xComponent, pose.y + heading.yComponent)
    }
}
