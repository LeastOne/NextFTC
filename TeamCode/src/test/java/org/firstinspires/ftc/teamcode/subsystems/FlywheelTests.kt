package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.pedropathing.math.Vector
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.configureHardwareTelemetry
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.`when`

class FlywheelTests : SubsystemTests() {
    lateinit var pedro: PedroComponent
    lateinit var follower: Follower
    var time = 0.0
    val restoreClocks = mutableListOf<() -> Unit>()

    @Before
    fun setUp() {
        // The library owns timing; override its private clocks only in this integration test.
        val testSeconds: () -> Double = { time }
        listOf(Flywheel.leftProfile, Flywheel.rightProfile, Flywheel.leftLimiter, Flywheel.rightLimiter).forEach { control ->
            val clock = control.javaClass.getDeclaredField("clock").apply { isAccessible = true }.get(control)
            val seconds = clock.javaClass.getDeclaredField("seconds").apply { isAccessible = true }
            val original = seconds.get(clock)
            seconds.set(clock, testSeconds)
            restoreClocks += { seconds.set(clock, original) }
        }
        follower = mock(Follower::class.java)
        pedro = PedroComponent { follower }.apply { preInit() }
        Flywheel.initializeHardware()
        configureHardwareTelemetry()
        listOf(Flywheel.motorLeft, Flywheel.motorRight).forEach { motor ->
            val type = mock(MotorConfigurationType::class.java)
            `when`(motor.motor.motorType).thenReturn(type)
            `when`(type.achieveableMaxTicksPerSecond).thenReturn(1000.0)
            `when`(motor.motor.velocity).thenReturn(0.0)
            `when`(motor.motor.getCurrent(AMPS)).thenReturn(0.0)
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
        Flywheel.PROFILE.maxAcceleration = 1.6
        Flywheel.PROFILE.maxJerk = 16.0
        Flywheel.CURRENT.maxCurrent = 5.0
        Flywheel.CURRENT.reductionRate = 2.0
        Flywheel.CURRENT.recoveryRate = 0.5
        Flywheel.initialize()
    }

    @After
    fun stopPedro() {
        Flywheel.stop()
        restoreClocks.forEach { it() }
        pedro.postStop()
    }

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
        clearInvocations(Flywheel.motorLeft.motor, Flywheel.motorRight.motor)
        Flywheel.VEL = 0.4
        Flywheel.periodic()
        time = 0.35
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(-200.0)
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(500.0)

        Flywheel.periodic()

        assertEquals(0.6, Flywheel.motorLeft.power, 1e-9)
        assertEquals(0.3, Flywheel.motorRight.power, 1e-9)
        assertEquals(-1, Flywheel.motorLeft.direction)
        assertEquals(1, Flywheel.motorRight.direction)
        verify(Flywheel.motorLeft.motor).power = -Flywheel.motorLeft.power
        verify(Flywheel.motorRight.motor).power = Flywheel.motorRight.power
        listOf(Flywheel.motorLeft, Flywheel.motorRight).forEach {
            verify(it.motor, never()).setVelocityPIDFCoefficients(anyDouble(), anyDouble(), anyDouble(), anyDouble())
            verify(it.motor, never()).velocity = anyDouble()
        }
    }

    @Test
    fun targetsAndPowerAreBoundedAndZeroTargetCoasts() {
        Flywheel.VEL = 2.0
        Flywheel.periodic()
        time = 1.0
        Flywheel.periodic()
        assertEquals(1.0, Flywheel.VEL, 0.0)
        assertEquals(1.0, Flywheel.motorLeft.power, 0.0)
        Flywheel.VEL = -2.0
        Flywheel.periodic()
        time = 3.0
        Flywheel.periodic()
        assertEquals(-1.0, Flywheel.VEL, 0.0)
        assertEquals(-1.0, Flywheel.motorRight.power, 0.0)
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(-300.0)
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
        time = 0.35
        Flywheel.periodic()
        assertEquals(0.4, Flywheel.motorLeft.power, 1e-9)
        assertEquals(0.4, Flywheel.motorRight.power, 1e-9)
    }

    @Test
    fun readinessRequiresBothMotorsWithinToleranceOfAPositiveTarget() {
        assertFalse(Flywheel.ready())
        Flywheel.VEL = 0.5
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(-300.0)
        assertFalse(Flywheel.ready())
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(-500.0)
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
        Flywheel.periodic()
        time = 1.0
        Flywheel.periodic()
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(6.0)
        time = 1.25
        Flywheel.periodic()
        assertEquals(0.5, Flywheel.leftLimiter.powerLimit, 0.0)

        Flywheel.stop()

        assertFalse(Flywheel.targeting)
        assertEquals(0.0, Flywheel.VEL, 0.0)
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.motorRight.power, 0.0)
        assertEquals(0.0, Flywheel.leftController.goal.velocity, 0.0)
        assertEquals(0.0, Flywheel.rightController.goal.velocity, 0.0)
        assertEquals(1.0, Flywheel.leftLimiter.powerLimit, 0.0)
        assertEquals(1.0, Flywheel.rightLimiter.powerLimit, 0.0)
    }

    @Test
    fun sCurveDoesNotDeclareReadinessAtAnIntermediateSpeed() {
        Flywheel.forward.start()
        Flywheel.periodic()
        time = 0.125
        `when`(Flywheel.motorLeft.motor.velocity).thenReturn(-120.0)
        `when`(Flywheel.motorRight.motor.velocity).thenReturn(120.0)
        Flywheel.periodic()
        assertEquals(0.12, Flywheel.leftController.reference.velocity, 1e-9)
        assertEquals(0.12, Flywheel.rightController.reference.velocity, 1e-9)
        assertEquals(0.12, Flywheel.motorLeft.power, 1e-9)
        assertFalse(Flywheel.ready())
        assertEquals(0.4, Flywheel.VEL, 0.0)
    }

    @Test
    fun sCurveSmoothsAccelerationAndRetargetsContinuously() {
        Flywheel.forward.start()
        Flywheel.periodic()
        time = 0.025
        Flywheel.periodic()
        val before = Flywheel.leftProfile.currentReference
        assertEquals(0.005, before.velocity, 1e-9)
        assertEquals(0.4, before.acceleration, 1e-9)
        Flywheel.reverse.start()
        Flywheel.periodic()
        assertEquals(before, Flywheel.leftProfile.currentReference)
        assertEquals(before, Flywheel.rightProfile.currentReference)
        time = 0.05
        Flywheel.periodic()
        assertEquals(0.01, Flywheel.leftProfile.currentReference.velocity, 1e-9)
        assertEquals(0.0, Flywheel.leftProfile.currentReference.acceleration, 1e-9)
        assertFalse(Flywheel.ready())
        Flywheel.stop.start()
        Flywheel.periodic()
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.leftProfile.currentReference.acceleration, 0.0)
    }

    @Test
    fun eachMotorLimitsItsOwnCurrentAndStopRestartsFromRestAfterIdle() {
        Flywheel.VEL = 1.0
        Flywheel.periodic()
        time = 1.0
        Flywheel.periodic()
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(6.0)
        time = 1.25
        Flywheel.periodic()
        assertEquals(0.5, Flywheel.motorLeft.power, 0.0)
        assertEquals(1.0, Flywheel.motorRight.power, 0.0)
        time = 1.5
        Flywheel.periodic()
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(0.0)
        time = 1.75
        Flywheel.periodic()
        assertEquals(0.125, Flywheel.motorLeft.power, 0.0)
        Flywheel.stop.start()
        Flywheel.periodic()
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.motorRight.power, 0.0)
        assertEquals(1.0, Flywheel.leftLimiter.powerLimit, 0.0)
        time = 100.0
        Flywheel.forward.start()
        Flywheel.periodic()
        assertEquals(0.0, Flywheel.motorLeft.power, 0.0)
        assertEquals(0.0, Flywheel.motorRight.power, 0.0)
        time = 100.125
        Flywheel.periodic()
        assertEquals(0.12, Flywheel.leftController.reference.velocity, 1e-9)
    }

    @Test
    fun settingsApplyToBothMotorsWithoutReinitialization() {
        Flywheel.PROFILE.maxAcceleration = 2.0
        Flywheel.PROFILE.maxJerk = 20.0
        Flywheel.CURRENT.maxCurrent = 4.0
        Flywheel.CURRENT.reductionRate = 1.0
        Flywheel.CURRENT.recoveryRate = 0.25
        Flywheel.VEL = 1.0
        Flywheel.periodic()
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(5.0)
        `when`(Flywheel.motorRight.motor.getCurrent(AMPS)).thenReturn(5.0)
        time = 0.1
        Flywheel.periodic()
        assertEquals(0.1, Flywheel.leftController.reference.velocity, 1e-9)
        assertEquals(0.1, Flywheel.rightController.reference.velocity, 1e-9)
        assertEquals(0.9, Flywheel.leftLimiter.powerLimit, 1e-9)
        assertEquals(0.9, Flywheel.rightLimiter.powerLimit, 1e-9)
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(0.0)
        time = 0.2
        Flywheel.periodic()
        assertEquals(0.925, Flywheel.leftLimiter.powerLimit, 1e-9)
    }

    @Test
    fun panelsStyleFieldEditsTuneRunningProfilesAndLimitersWithoutResettingState() {
        val leftController = Flywheel.leftController
        val rightController = Flywheel.rightController
        val leftLimiter = Flywheel.leftLimiter
        val rightLimiter = Flywheel.rightLimiter
        Flywheel.VEL = 1.0
        Flywheel.periodic()
        time = 0.125
        Flywheel.periodic()
        assertEquals(0.12, Flywheel.leftProfile.currentReference.velocity, 1e-9)

        // Panels writes backing fields rather than invoking Kotlin setters.
        mapOf("maxAcceleration" to 3.2, "maxJerk" to 32.0).forEach { (name, value) ->
            Flywheel.PROFILE.javaClass.getDeclaredField(name).apply { isAccessible = true }.setDouble(Flywheel.PROFILE, value)
        }
        mapOf("maxCurrent" to 4.0, "reductionRate" to 1.0, "recoveryRate" to 1.0).forEach { (name, value) ->
            Flywheel.CURRENT.javaClass.getDeclaredField(name).apply { isAccessible = true }.setDouble(Flywheel.CURRENT, value)
        }
        assertSame(Flywheel.PROFILE, Flywheel.leftProfile.parameters)
        assertSame(Flywheel.PROFILE, Flywheel.rightProfile.parameters)
        assertSame(Flywheel.CURRENT, leftLimiter.parameters)
        assertSame(Flywheel.CURRENT, rightLimiter.parameters)
        assertEquals(3.2, Flywheel.leftProfile.parameters.maxAcceleration, 0.0)
        assertEquals(3.2, Flywheel.rightProfile.parameters.maxAcceleration, 0.0)
        assertEquals(32.0, Flywheel.leftProfile.parameters.maxJerk, 0.0)
        assertEquals(32.0, Flywheel.rightProfile.parameters.maxJerk, 0.0)
        listOf(leftLimiter, rightLimiter).forEach {
            assertEquals(4.0, it.parameters.maxCurrent, 0.0)
            assertEquals(1.0, it.parameters.reductionRate, 0.0)
            assertEquals(1.0, it.parameters.recoveryRate, 0.0)
        }
        Flywheel.periodic()
        assertEquals(0.12, Flywheel.leftProfile.currentReference.velocity, 1e-9)
        assertEquals(0.12, Flywheel.rightProfile.currentReference.velocity, 1e-9)
        `when`(Flywheel.motorLeft.motor.getCurrent(AMPS)).thenReturn(5.0)
        `when`(Flywheel.motorRight.motor.getCurrent(AMPS)).thenReturn(5.0)
        time = 0.25
        Flywheel.periodic()
        assertEquals(0.48, Flywheel.leftProfile.currentReference.velocity, 1e-9)
        assertEquals(0.48, Flywheel.rightProfile.currentReference.velocity, 1e-9)
        assertEquals(0.875, Flywheel.motorLeft.power, 1e-9)
        assertEquals(0.875, Flywheel.motorRight.power, 1e-9)

        Flywheel.CURRENT.maxCurrent = 10.0
        Flywheel.CURRENT.recoveryRate = 0.5
        Flywheel.periodic()
        assertEquals(0.875, Flywheel.leftLimiter.powerLimit, 1e-9)
        time = 0.375
        Flywheel.periodic()
        assertEquals(0.9375, Flywheel.leftLimiter.powerLimit, 1e-9)
        assertEquals(0.9375, Flywheel.rightLimiter.powerLimit, 1e-9)
        Flywheel.CURRENT.maxCurrent = 4.0
        Flywheel.CURRENT.reductionRate = 2.0
        time = 0.5
        Flywheel.periodic()
        assertEquals(0.6875, Flywheel.leftLimiter.powerLimit, 1e-9)
        assertEquals(0.6875, Flywheel.rightLimiter.powerLimit, 1e-9)
        assertSame(leftController, Flywheel.leftController)
        assertSame(rightController, Flywheel.rightController)
        assertSame(leftLimiter, Flywheel.leftLimiter)
        assertSame(rightLimiter, Flywheel.rightLimiter)
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
