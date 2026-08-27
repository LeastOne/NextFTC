package org.firstinspires.ftc.threedrd.nextftc.subsystems

import android.content.Context
import android.content.pm.ApplicationInfo
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import dalvik.system.DexFile
import dev.nextftc.core.commands.Command
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.core.subsystems.Subsystem as NextSubsystem
import java.util.Collections
import org.firstinspires.ftc.threedrd.nextftc.hardware.Hardware
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.ERROR
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.DiscoveredSubsystem
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SubsystemComponentTests {
    @TeleOp
    private class TestTeleop : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @Autonomous
    private class TestAuto : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    private class TestHardware(
        override val name: String,
        val failure: Exception? = null
    ) : Hardware {
        var initializations = 0

        override fun initialize() {
            initializations++
            failure?.let { throw it }
        }
    }

    private class PersistentCommand(requirement: Any) : Command() {
        override val isDone = false

        init {
            requires(requirement)
        }
    }

    private class OneShotCommand(requirement: Any) : Command() {
        override val isDone = true

        init {
            requires(requirement)
        }
    }

    private class AdaptedSubsystem(val hardware: TestHardware) : Subsystem() {
        var initializations = 0
        var starts = 0
        var controls = 0
        var stops = 0
        var updates = 0
        val idle = PersistentCommand(this)
        override val defaultCommand get() = idle

        override fun initialize() {
            initializations++
        }

        override fun periodic() {
            updates++
        }

        override fun start() {
            starts++
        }

        override fun controls() {
            controls++
        }

        override fun stop() {
            stops++
        }
    }

    private class NativeSubsystem : NextSubsystem {
        var initializations = 0
        var updates = 0

        override fun initialize() {
            initializations++
        }

        override fun periodic() {
            updates++
        }
    }

    private class OrderedSubsystem(
        override val order: Int,
        val updates: MutableList<Int>
    ) : Subsystem() {
        override fun periodic() {
            updates += order
        }
    }

    private lateinit var telemetry: Telemetry

    @Before
    fun setUp() {
        CommandManager.cancelAll()
        CommandManager.run()
        telemetry = mock(Telemetry::class.java)
        val telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        `when`(telemetryLog.capacity).thenReturn(9)
        TeamTelemetry.output = telemetry
        TeamTelemetry.LEVEL = ERROR
        TeamTelemetry.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply {
            hardwareMap = mock(HardwareMap::class.java)
            this.telemetry = this@SubsystemComponentTests.telemetry
        }
    }

    @After
    fun tearDown() {
        CommandManager.cancelAll()
        CommandManager.run()
        SubsystemComponent.discovery = SubsystemComponent::discover
        SubsystemComponent.discovered = null
    }

    @Test
    fun initializesAndUpdatesReadyAndNativeSubsystems() {
        activate(TestTeleop())
        val adapted = AdaptedSubsystem(TestHardware("ready"))
        val native = NativeSubsystem()
        val component = SubsystemComponent(adapted, native)
        assertFalse(component.discoverAll)
        component.subsystems = component.subsystems

        component.preInit()
        component.preWaitForStart()
        component.preStartButtonPressed()
        CommandManager.run()
        component.preUpdate()
        CommandManager.run()
        component.preStop()

        assertEquals(setOf(adapted, native), component.subsystems)
        assertEquals(1, adapted.hardware.initializations)
        assertEquals(1, adapted.initializations)
        assertEquals(1, adapted.starts)
        assertEquals(1, adapted.controls)
        assertEquals(1, adapted.stops)
        assertEquals(2, adapted.updates)
        assertEquals(1, native.initializations)
        assertEquals(2, native.updates)
        assertTrue(CommandManager.hasCommandsUsing(adapted))
    }

    @Test
    fun startsWithoutCreatingDriverControlsDuringAutonomous() {
        activate(TestAuto())
        val subsystem = AdaptedSubsystem(TestHardware("ready"))

        SubsystemComponent(subsystem).preStartButtonPressed()

        assertEquals(1, subsystem.starts)
        assertEquals(0, subsystem.controls)
    }

    @Test
    fun skipsDisabledSubsystemLifecycle() {
        val subsystem = AdaptedSubsystem(
            TestHardware("bad", IllegalStateException("missing"))
        )
        val component = SubsystemComponent(subsystem)

        component.preInit()
        component.preStartButtonPressed()
        component.preUpdate()
        component.preStop()

        assertTrue(subsystem.disabled)
        assertEquals(0, subsystem.initializations)
        assertEquals(0, subsystem.starts)
        assertEquals(0, subsystem.stops)
        assertEquals(0, subsystem.updates)
        assertFalse(CommandManager.hasCommandsUsing(subsystem))
        verify(telemetry).addData(
            "E | AdaptedSubsystem | Status", "Disabled (see Logcat)" as Any
        )
    }

    @Test
    fun doesNotReplaceACommandAlreadyUsingTheSubsystem() {
        val subsystem = AdaptedSubsystem(TestHardware("ready"))
        val active = PersistentCommand(subsystem)
        val component = SubsystemComponent(subsystem)
        active.schedule()
        CommandManager.run()

        component.preInit()
        component.preUpdate()
        CommandManager.run()

        assertTrue(active.isScheduled)
        assertFalse(subsystem.idle.isScheduled)
        verify(telemetry, times(0)).addData(
            "E | AdaptedSubsystem | Status", "Disabled (see Logcat)" as Any
        )
    }

    @Test
    fun schedulesDefaultsOnlyDuringActiveUpdates() {
        val subsystem = AdaptedSubsystem(TestHardware("ready"))
        val component = SubsystemComponent(subsystem)

        component.preWaitForStart()
        CommandManager.run()
        assertFalse(subsystem.idle.isScheduled)

        component.preUpdate()
        CommandManager.run()
        assertTrue(subsystem.idle.isScheduled)
    }

    @Test
    fun resumesDefaultAfterAnotherCommandFinishes() {
        val subsystem = AdaptedSubsystem(TestHardware("ready"))
        val component = SubsystemComponent(subsystem)
        component.preUpdate()
        CommandManager.run()
        assertTrue(subsystem.idle.isScheduled)

        OneShotCommand(subsystem).schedule()
        CommandManager.run()
        assertFalse(subsystem.idle.isScheduled)
        CommandManager.run()

        component.preUpdate()
        CommandManager.run()
        assertTrue(subsystem.idle.isScheduled)
    }

    @Test
    fun defaultControlsRequireNoSpecialHandling() {
        val subsystem = object : Subsystem() {}
        subsystem.controls()
    }

    @Test
    fun updatesSubsystemsInExplicitOrder() {
        val updates = mutableListOf<Int>()
        val component = SubsystemComponent(
            OrderedSubsystem(1, updates),
            OrderedSubsystem(-1, updates),
            OrderedSubsystem(0, updates)
        )

        component.preUpdate()

        assertEquals(listOf(-1, 0, 1), updates)
    }

    @Test
    fun stopsAdaptedSubsystemsInReverseOrder() {
        val stops = mutableListOf<Int>()
        val disabled = object : Subsystem() {
            override val order = 2
            override fun stop() { stops += order }
        }.apply { errors += "unavailable" }
        val component = SubsystemComponent(
            object : Subsystem() {
                override val order = -1
                override fun stop() { stops += order }
            },
            object : Subsystem() {
                override val order = 1
                override fun stop() { stops += order }
            },
            disabled
        )

        component.preStop()

        assertEquals(listOf(1, -1), stops)
    }

    @Test
    fun continuesStoppingAfterASubsystemFails() {
        val stops = mutableListOf<Int>()
        val component = SubsystemComponent(
            object : Subsystem() {
                override val order = -1
                override fun stop() { stops += order }
            },
            object : Subsystem() {
                override val order = 0
                override fun stop() {
                    stops += order
                    error("failed")
                }
            },
            object : Subsystem() {
                override val order = 1
                override fun stop() { stops += order }
            }
        )

        component.preStop()

        assertEquals(listOf(1, 0, -1), stops)
    }

    @Test
    fun defaultStopRequiresNoSpecialHandling() {
        object : Subsystem() {}.stop()
    }

    @Test
    fun discoversSubsystemsOnlyOnce() {
        val subsystem = AdaptedSubsystem(TestHardware("ready"))
        var discoveries = 0
        SubsystemComponent.discovered = null
        SubsystemComponent.discovery = {
            discoveries++
            setOf(subsystem)
        }
        assertNotNull(SubsystemComponent.discovery)

        val first = SubsystemComponent.all()
        val second = SubsystemComponent.all()
        assertTrue(first.discoverAll)
        first.preInit()
        second.preInit()

        assertEquals(setOf(subsystem), first.subsystems)
        assertEquals(setOf(subsystem), second.subsystems)
        assertEquals(setOf(subsystem), SubsystemComponent.discovered)
        assertEquals(1, discoveries)
    }

    @Suppress("DEPRECATION")
    @Test
    fun discoversSubsystemsFromTheActiveRobotController() {
        val context = mock(Context::class.java)
        val applicationInfo = ApplicationInfo().apply { sourceDir = "robot.apk" }
        `when`(context.applicationInfo).thenReturn(applicationInfo)
        val hardwareMap = HardwareMap(
            context,
            mock(OpModeManagerNotifier::class.java)
        )
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.hardwareMap = hardwareMap }

        mockConstruction(DexFile::class.java) { dex, _ ->
            `when`(dex.entries()).thenReturn(
                Collections.enumeration(listOf(DiscoveredSubsystem::class.java.name))
            )
        }.use {
            assertEquals(setOf(DiscoveredSubsystem), SubsystemComponent.discover())
        }
    }

    private fun activate(opMode: LinearOpMode) {
        ActiveOpMode.it = opMode.apply {
            hardwareMap = mock(HardwareMap::class.java)
            telemetry = this@SubsystemComponentTests.telemetry
        }
    }
}
