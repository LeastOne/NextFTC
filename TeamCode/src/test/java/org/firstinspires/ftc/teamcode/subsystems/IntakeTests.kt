package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class IntakeTests : SubsystemTests() {
    lateinit var pedro: PedroComponent
    lateinit var follower: Follower
    lateinit var motor: DcMotorEx

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        pedro = PedroComponent { follower }.apply { preInit() }
        Intake.initializeHardware()
        motor = Intake.motor.motor
        val type = mock(MotorConfigurationType::class.java)
        `when`(motor.motorType).thenReturn(type)
        `when`(type.achieveableMaxTicksPerSecond).thenReturn(1000.0)
        `when`(follower.pose).thenReturn(Pose())
        Intake.laserDebounce.clock = { 1.0 }
        Intake.initialize()
        config.alliance = BLUE
        state.started = false
    }

    @After
    fun stopPedro() = pedro.postStop()

    @Test
    fun commandsControlVelocityAndResetArtifacts() {
        Intake.forward.start()
        assertEquals(Intake.FWD, Intake.VEL, 0.0)
        Intake.reverse.start()
        assertEquals(Intake.REV, Intake.VEL, 0.0)
        Intake.hold.start()
        assertEquals(Intake.HOLD, Intake.VEL, 0.0)
        Intake.artifacts = 2
        Intake.full = true
        Intake.reset.start()
        assertEquals(0, Intake.artifacts)
        assertFalse(Intake.full)
        Intake.stop.start()
        assertEquals(Intake.STOP, Intake.VEL, 0.0)
    }

    @Test
    fun periodicRunsHardwareAndCountsDebouncedArtifacts() {
        val laser = Intake.laser
        `when`(laser.state).thenReturn(true)
        Intake.laserDebounce.changedAt = 0.0
        state.started = true
        Intake.VEL = Intake.FWD
        clearInvocations(motor, Intake.bumperLeft.servo, Intake.bumperRight.servo)

        Intake.periodic()

        verify(motor).velocity = 1000.0
        assertEquals(1.0, Intake.bumperLeft.position, 0.0)
        assertEquals(1.0, Intake.bumperRight.position, 0.0)
        assertEquals(1, Intake.artifacts)
        assertFalse(Intake.full)

        Intake.laserDebounce.previous = false
        Intake.laserDebounce.changedAt = 0.0
        Intake.artifacts = Intake.MAX_ARTIFACTS - 1
        Intake.periodic()
        assertTrue(Intake.full)
        assertEquals(Intake.MAX_ARTIFACTS, Intake.artifacts)

        Intake.laserDebounce.previous = false
        Intake.laserDebounce.changedAt = 0.0
        Intake.periodic()
        assertEquals(Intake.MAX_ARTIFACTS, Intake.artifacts)
    }

    @Test
    fun bumpersReflectAllianceNearTheGateAndRetractOtherwise() {
        config.alliance = RED
        `when`(follower.pose).thenReturn(Nav.gateIntake)
        state.started = true
        Intake.VEL = Intake.FWD

        Intake.periodic()
        assertEquals(1.0, Intake.BUMPER_LEFT_POS, 0.0)
        assertEquals(1.0, Intake.BUMPER_RIGHT_POS, 0.0)

        config.alliance = BLUE
        `when`(follower.pose).thenReturn(Nav.gateIntake)
        Intake.periodic()
        assertEquals(0.0, Intake.BUMPER_LEFT_POS, 0.0)

        Intake.VEL = Intake.HOLD
        Intake.periodic()
        assertEquals(1.0, Intake.bumperLeft.position, 0.0)
        assertEquals(0.0, Intake.bumperRight.position, 0.0)

        `when`(follower.pose).thenReturn(Pose(100.0, -10.0))
        state.started = false
        Intake.periodic()
        assertEquals(1.0, Intake.bumperLeft.position, 0.0)
        assertEquals(0.0, Intake.bumperRight.position, 0.0)
    }

    @Test
    fun waitCommandsObserveArtifactState() {
        Intake.artifacts = 1
        val element = Intake.untilElement()
        element.start()
        assertFalse(element.isDone)
        Intake.artifacts = 2
        assertTrue(element.isDone)
        element.stop(false)

        Intake.artifacts = 1
        Intake.full = true
        val fullElement = Intake.untilElement()
        fullElement.start()
        assertTrue(fullElement.isDone)

        Intake.full = false
        val full = Intake.untilFull()
        assertFalse(full.isDone)
        Intake.full = true
        assertTrue(full.isDone)
        Intake.artifacts = 2
        assertTrue(Intake.untilArtifacts(2).isDone)
        assertFalse(Intake.untilArtifacts(3).isDone)
    }

    @Test
    fun configurableValuesRemainMutable() {
        Intake.STOP = Intake.STOP
        Intake.HOLD = Intake.HOLD
        Intake.FWD = Intake.FWD
        Intake.REV = Intake.REV
        Intake.MAX_ARTIFACTS = Intake.MAX_ARTIFACTS
        Intake.LASER_THRESHOLD = Intake.LASER_THRESHOLD
        Intake.BUMPER_LEFT_POS = Intake.BUMPER_LEFT_POS
        Intake.BUMPER_LEFT_MIN = Intake.BUMPER_LEFT_MIN
        Intake.BUMPER_LEFT_MAX = Intake.BUMPER_LEFT_MAX
        Intake.BUMPER_RIGHT_POS = Intake.BUMPER_RIGHT_POS
        Intake.BUMPER_RIGHT_MIN = Intake.BUMPER_RIGHT_MIN
        Intake.BUMPER_RIGHT_MAX = Intake.BUMPER_RIGHT_MAX
    }

    @Test
    fun lifecycleStopImmediatelyStopsTheMotor() {
        Intake.VEL = 1.0

        Intake.stop()

        assertEquals(0.0, Intake.VEL, 0.0)
        assertEquals(0.0, Intake.motor.power, 0.0)
    }

    @Test
    fun controlsCreateDriverBindings() {
        Intake.controls()
    }
}
