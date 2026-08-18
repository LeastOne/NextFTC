package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose

/** Establishes a new starting frame and places the robot exactly at its origin. */
fun Follower.resetStartingPose(pose: Pose) {
    setStartingPose(pose)
    setPose(pose)
}
