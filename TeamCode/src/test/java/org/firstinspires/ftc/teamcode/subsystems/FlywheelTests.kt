package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.math.Vector
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.configureHardwareTelemetry
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.`when`

class FlywheelTests : SubsystemTests() {
    lateinit var pedro: PedroComponent
    lateinit var follower: Follower

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        pedro = PedroComponent { follower }.apply { preInit() }
        Flywheel.initializeHardware()
        configureHardwareTelemetry()
        listOf(Flywheel.motorLeft, Flywheel.motorRight).forEach { motor ->
            val type = mock(MotorConfigurationType::class.java)
            `when`(motor.motor.motorType).thenReturn(type)
            `when`(type.achieveableMaxTicksPerSecond).thenReturn(1000.0)
            `when`(motor.motor.velocity).thenReturn(0.0)
        }
        `when`(follower.pose).thenReturn(Pose())
        `when`(follower.velocity).thenReturn(Vector())
        `when`(follower.acceleration).thenReturn(Vector())
        Config.alliance = BLUE
        Flywheel.PID.kP = 1.0
        Flywheel.PID.kI = 0.0
        Flywheel.PID.kD = 0.0
        Flywheel.FF.kV = 1.0
        Flywheel.FF.kA = 0.0
        Flywheel.FF.kS = 0.0
        Flywheel.AXIAL_KS = 0.0
        Flywheel.AXIAL_KV = 0.0008
        Flywheel.AXIAL_KA = 0.0
        Flywheel.initialize()
    }

    @After
    fun stopPedro() = pedro.postStop()

    @Test
    fun commandsSelectFixedAndTargetedModes() {
        Flywheel.forward.start()
        assertEquals(Flywheel.FWD, Flywheel.VEL, 0.0)
        assertFalse(Flywheel.targeting)
        Flywheel.reverse.start()
        assertEquals(Flywheel.REV, Flywheel.VEL, 0.0)
        Flywheel.hold.start()
        assertEquals(Flywheel.HOLD, Flywheel.VEL, 0.0)
        Flywheel.launch.start()
        assertTrue(Flywheel.targeting)
        Flywheel.stop.start()
        assertFalse(Flywheel.targeting)
        assertEquals(Flywheel.STOP, Flywheel.VEL, 0.0)
    }

    @Test
    fun calculatesTargetVelocityWithAxialFeedforward() {
        val velocity = Vector(2.0, 0.0)
        val acceleration = Vector(3.0, 0.0)
        `when`(follower.velocity).thenReturn(velocity)
        `when`(follower.acceleration).thenReturn(acceleration)
        Flywheel.AXIAL_KS = 0.1
        Flywheel.AXIAL_KV = 0.2
        Flywheel.AXIAL_KA = 0.3
        Flywheel.targeting = true

        val targeted = Flywheel.calculateVelocity()
        Flywheel.targeting = false
        Flywheel.VEL = 0.25

        assertTrue(targeted > 1.0)
        assertEquals(0.25, Flywheel.calculateVelocity(), 0.0)
    }

    @Test
    fun periodicControlsEachMotorFromItsOwnFeedback() {
        Flywheel.VEL = 0.4
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(200.0)
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(500.0)

        Flywheel.periodic()

        assertEquals(0.6, Flywheel.motorLeft.power, 1e-9)
        assertEquals(0.3, Flywheel.motorRight.power, 1e-9)
        listOf(Flywheel.motorLeft, Flywheel.motorRight).forEach {
            verify(it.motor, never()).setVelocityPIDFCoefficients(anyDouble(), anyDouble(), anyDouble(), anyDouble())
            verify(it.motor, never()).velocity = anyDouble()
        }
    }

    @Test
    fun targetsAndPowerAreBoundedAndZeroTargetCoasts() {
        Flywheel.VEL = 2.0
        Flywheel.periodic()
        assertEquals(1.0, Flywheel.VEL, 0.0)
        assertEquals(1.0, Flywheel.motorLeft.power, 0.0)
        Flywheel.VEL = -2.0
        Flywheel.periodic()
        assertEquals(-1.0, Flywheel.VEL, 0.0)
        assertEquals(-1.0, Flywheel.motorRight.power, 0.0)
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(300.0)
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(300.0)
        Flywheel.stop.start()
        Flywheel.periodic()
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.motorRight.power, 0.0)
    }

    @Test
    fun coefficientEditsApplyToBothExistingControllers() {
        Flywheel.PID.kP = 0.5
        Flywheel.FF.kV = 0.5
        Flywheel.VEL = 0.4
        Flywheel.periodic()
        assertEquals(0.4, Flywheel.motorLeft.power, 1e-9)
        assertEquals(0.4, Flywheel.motorRight.power, 1e-9)
    }

    @Test
    fun readinessRequiresBothMotorsWithinToleranceOfAPositiveTarget() {
        assertFalse(Flywheel.ready())
        Flywheel.VEL = 0.5
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(300.0)
        assertFalse(Flywheel.ready())
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(500.0)
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(300.0)
        assertFalse(Flywheel.ready())
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(500.0)
        assertTrue(Flywheel.ready())
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(700.0)
        assertFalse(Flywheel.ready())
        assertFalse(Flywheel.untilReady().isDone)
    }

    @Test
    fun lifecycleStopStopsBothMotors() {
        Flywheel.targeting = true
        Flywheel.VEL = 1.0

        Flywheel.stop()

        assertFalse(Flywheel.targeting)
        assertEquals(0.0, Flywheel.VEL, 0.0)
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.motorRight.power, 0.0)
    }

    @Test
    fun controlsCreateDriverBindings() {
        Flywheel.controls()
    }

    @Test
    fun configurableValuesRemainMutable() {
        Flywheel.AXIAL_KS = Flywheel.AXIAL_KS
        Flywheel.AXIAL_KV = Flywheel.AXIAL_KV
        Flywheel.AXIAL_KA = Flywheel.AXIAL_KA
        Flywheel.FWD = Flywheel.FWD
        Flywheel.REV = Flywheel.REV
        Flywheel.HOLD = Flywheel.HOLD
        Flywheel.STOP = Flywheel.STOP
        Flywheel.TOLERANCE = Flywheel.TOLERANCE
    }
}
