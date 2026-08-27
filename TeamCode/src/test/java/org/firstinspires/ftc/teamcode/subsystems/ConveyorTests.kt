package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ConveyorTests : SubsystemTests() {
    lateinit var pedro: PedroComponent
    lateinit var follower: Follower

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        pedro = PedroComponent { follower }.apply { preInit() }
        Conveyor.initializeHardware()
        val type = mock(MotorConfigurationType::class.java)
        `when`(Conveyor.motor.motor.motorType).thenReturn(type)
        `when`(type.achieveableMaxTicksPerSecond).thenReturn(1000.0)
        `when`(follower.pose).thenReturn(Pose())
        config.alliance = BLUE
        Conveyor.initialize()
    }

    @After
    fun stopPedro() = pedro.postStop()

    @Test
    fun commandsSelectEachOperatingMode() {
        Conveyor.forward.start()
        assertEquals(Conveyor.FWD, Conveyor.VEL, 0.0)
        Conveyor.reverse.start()
        assertEquals(Conveyor.REV, Conveyor.VEL, 0.0)
        Conveyor.launch.start()
        assertEquals(Conveyor.calculateVelocity(), Conveyor.VEL, 0.0)
        Conveyor.stop.start()
        assertTrue(Conveyor.stopped())
    }

    @Test
    fun periodicAppliesVelocityAndLifecycleStopStopsImmediately() {
        Conveyor.VEL = 0.4

        Conveyor.periodic()
        verify(Conveyor.motor.motor).velocity = 400.0

        Conveyor.stop()
        assertEquals(0.0, Conveyor.VEL, 0.0)
        assertEquals(0.0, Conveyor.motor.power, 0.0)
    }

    @Test
    fun waitObservesStoppedState() {
        Conveyor.VEL = Conveyor.FWD
        val wait = Conveyor.untilStopped()
        assertFalse(wait.isDone)
        Conveyor.VEL = Conveyor.STOP
        assertTrue(wait.isDone)
    }

    @Test
    fun controlsCreateDriverBindings() {
        Conveyor.controls()
    }
}
