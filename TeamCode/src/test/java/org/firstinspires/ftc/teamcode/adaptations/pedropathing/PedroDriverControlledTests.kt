package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import com.pedropathing.follower.Follower
import dev.nextftc.extensions.pedro.PedroComponent
import java.util.function.Supplier
import org.junit.After
import org.junit.Test
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class PedroDriverControlledTests : SubsystemTests() {
    val follower = mock(Follower::class.java)
    val component = PedroComponent { follower }.apply { preInit() }

    @After
    fun tearDown() = component.postStop()

    @Test
    fun appliesScalarAndCurrentCentricMode() {
        var robotCentric = false
        val command = PedroDriverControlled(
            Supplier { 0.5 },
            Supplier { -0.25 },
            Supplier { 1.0 },
            { robotCentric }
        ).apply { scalar = 0.5 }

        command.start()
        command.update()
        robotCentric = true
        command.update()

        verify(follower).startTeleopDrive()
        verify(follower).setTeleOpDrive(0.25, -0.125, 0.5, false, 0.0)
        verify(follower).setTeleOpDrive(0.25, -0.125, 0.5, true, 0.0)
    }

    @Test
    fun breaksFollowingOnlyWhenInterrupted() {
        val command = PedroDriverControlled(
            Supplier { 0.0 },
            Supplier { 0.0 },
            Supplier { 0.0 },
            { false }
        )

        command.stop(false)
        verify(follower, never()).breakFollowing()

        command.stop(true)
        verify(follower).breakFollowing()
    }
}
