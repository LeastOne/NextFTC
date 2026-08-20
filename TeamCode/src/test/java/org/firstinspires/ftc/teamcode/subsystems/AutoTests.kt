package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.core.commands.groups.ParallelGroup
import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.ArgumentCaptor

class AutoTests {
    lateinit var follower: Follower
    lateinit var component: PedroComponent

    @Before
    fun setUp() {
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { hardwareMap = mock(HardwareMap::class.java) }
        follower = mock(Follower::class.java)
        component = PedroComponent { follower }
        component.preInit()
        Config.config.delay = 0.0
    }

    @After
    fun tearDown() {
        component.postStop()
    }

    @Test
    fun locateResetsPedrosStartingPoseExactly() {
        val pose = Nav.start
        Auto.locate.start()

        val poses = ArgumentCaptor.forClass(com.pedropathing.geometry.Pose::class.java)
        inOrder(follower).run {
            verify(follower).setStartingPose(poses.capture())
            verify(follower).setPose(poses.capture())
        }
        poses.allValues.forEach {
            assertEquals(pose.x, it.x, 0.0)
            assertEquals(pose.y, it.y, 0.0)
            assertEquals(pose.heading, it.heading, 0.0)
        }
        assertEquals("Auto.locate", Auto.locate.name)
    }

    @Test
    fun executeCreatesAComposableSampleRoutine() {
        val first = Auto.execute()
        val second = Auto.execute()

        assertNotSame(first, second)
        assertTrue(first.requirements.containsAll(listOf(Auto, Drive, Gate, Deflector)))

        val commands = first.commands
        assertEquals(Auto.locate, commands.first())
        assertEquals(2, commands.filterIsInstance<ParallelGroup>().size)
        assertTrue(commands.any { it.name.contains("Gate.open") })
    }
}
