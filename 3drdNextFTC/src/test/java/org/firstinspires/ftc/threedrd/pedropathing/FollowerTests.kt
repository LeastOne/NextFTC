package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock

class FollowerTests {
    @Test
    fun resetStartingPoseEstablishesTheFrameBeforeTheExactPose() {
        val follower = mock(Follower::class.java)
        val pose = Pose(1.0, 2.0, 3.0)

        follower.resetStartingPose(pose)

        inOrder(follower).run {
            verify(follower).setStartingPose(pose)
            verify(follower).setPose(pose)
        }
    }
}
