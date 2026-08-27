package org.firstinspires.ftc.threedrd.nextftc.subsystems

import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.FuturePose
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.Path
import com.pedropathing.paths.PathBuilder
import com.pedropathing.paths.PathChain
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.core.units.Angle
import dev.nextftc.core.units.Distance
import dev.nextftc.core.units.deg
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.FollowPath
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.extensions.pedro.TurnBy
import org.firstinspires.ftc.threedrd.nextftc.commands.DeferredCommand
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.threedrd.pedropathing.PathCompletion
import org.firstinspires.ftc.threedrd.pedropathing.PathT
import org.firstinspires.ftc.threedrd.pedropathing.axial
import org.firstinspires.ftc.threedrd.pedropathing.lateral
import org.firstinspires.ftc.threedrd.pedropathing.midpoint

abstract class DriveSubsystem : Subsystem() {
    protected open val headingEnd = 0.33

    val hold by instant { follower.holdPoint(follower.pose) }
    val stop by instant { follower.breakFollowing() }

    override fun stop() {
        follower.breakFollowing()
    }

    fun follow(path: Path, holdEnd: Boolean? = null, maxPower: Double? = null) =
        FollowPath(path, holdEnd, maxPower).requires(this)

    fun follow(path: PathChain, holdEnd: Boolean? = null, maxPower: Double? = null) =
        FollowPath(path, holdEnd, maxPower).requires(this)

    fun paths(holdEnd: Boolean = false, build: PathBuilder.() -> Unit) =
        DeferredCommand(this) {
            val builder = follower.pathBuilder().apply(build)
            follow(builder.build(), holdEnd)
        }.named("${javaClass.simpleName}.paths")

    fun to(pose: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        val start = follower.pose
        addPath(BezierCurve(start, start.midpoint(pose), pose))
        setLinearHeadingInterpolation(start.heading, pose.heading, headingEnd)
    }.named("${javaClass.simpleName}.to")

    fun curve(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        val start = follower.pose
        val points = mutableListOf<FuturePose>(start)
        points.addAll(poses)
        if (points.size < 3) points.add(1, start.midpoint(poses.last()))
        addPath(BezierCurve(*points.toTypedArray()))
        setLinearHeadingInterpolation(start.heading, poses.last().heading, headingEnd)
    }.named("${javaClass.simpleName}.curve")

    fun curves(vararg poses: Pose, holdEnd: Boolean = true) = paths(holdEnd) {
        var start = follower.pose
        poses.forEach { end ->
            addPath(BezierCurve(start, start.midpoint(end), end))
            setLinearHeadingInterpolation(start.heading, end.heading, headingEnd)
            start = end
        }
    }.named("${javaClass.simpleName}.curves")

    fun forward(distance: Distance) = DeferredCommand(this) {
        to(follower.pose.axial(distance.inIn))
    }.named("${javaClass.simpleName}.forward")

    fun forward(distance: Double) = forward(distance.inches)

    fun strafe(distance: Distance) = DeferredCommand(this) {
        to(follower.pose.lateral(distance.inIn))
    }.named("${javaClass.simpleName}.strafe")

    fun strafe(distance: Double) = strafe(distance.inches)

    fun turn(angle: Angle) = TurnBy(angle).requires(this).named("${javaClass.simpleName}.turn")

    fun turn(degrees: Double) = turn(degrees.deg)

    fun until(distance: Distance) = WaitUntil {
        if (distance.inIn >= 0) follower.distanceTraveledOnPath >= distance.inIn
        else follower.distanceRemaining < -distance.inIn
    }

    fun until(completion: PathCompletion) = WaitUntil {
        if (completion.value >= 0) follower.pathCompletion >= completion.value
        else follower.pathCompletion < 1 + completion.value
    }

    fun until(t: PathT) = WaitUntil {
        if (t.value >= 0) follower.currentTValue >= t.value
        else follower.currentTValue < 1 + t.value
    }

    fun untilNotBusy() = WaitUntil { !follower.isBusy }
}

