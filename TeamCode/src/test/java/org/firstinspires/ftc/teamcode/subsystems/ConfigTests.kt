package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Setting
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.DiagnosticsConfig.Level.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.DiagnosticsConfig.Level.WARN
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem.Change.NEXT
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem.Change.PREV
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.TelemetryComponent as TeamTelemetry
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.SOUTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.lang.reflect.Modifier

class ConfigTests : SubsystemTests() {
    lateinit var pedro: PedroComponent

    @Before
    fun resetConfig() {
        config.alliance = UNKNOWN
        config.side = UNKNOWN_SIDE
        config.quanomous = null
        config.delay = 0.0
        config.responsiveness = 1.0
        config.robotCentric = false
        config.level = INFO
        config.filter = ""
        state.auto = false
        state.teleop = false
        state.started = false
        state.interrupt = false
        state.setting = 0
        state.configurable = false
        Logging.FILTER = ""
        TeamTelemetry.FILTER = ""
        ConfigComponent.onChange = {}
        pedro = PedroComponent { mock(Follower::class.java) }.apply { preInit() }
    }

    @After
    fun stopPedro() {
        pedro.postStop()
    }

    @Test
    fun initializesTeleopRuntimeStateAndControls() {
        ActiveOpMode.it = TestTeleop()
        state.started = true
        state.setting = 4
        state.configurable = true

        Config.initialize()

        assertFalse(state.auto)
        assertTrue(state.teleop)
        assertTrue(state.interrupt)
        assertFalse(state.started)
        assertEquals(0, state.setting)
        assertFalse(state.configurable)
    }

    @TeleOp
    class TestTeleop : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @Autonomous
    class TestAuto : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @Test
    fun recognizesAutonomousOpModes() {
        ActiveOpMode.it = TestAuto()

        Config.initialize()

        assertTrue(state.auto)
        assertFalse(state.teleop)
        assertFalse(state.interrupt)
    }

    @Test
    fun configurationAndRuntimeStateCanBeUpdatedDirectly() {
        config.alliance = BLUE
        config.side = SOUTH
        config.quanomous = "routine"
        config.delay = 1.0
        config.responsiveness = 0.5
        config.robotCentric = true

        assertEquals(BLUE, config.alliance)
        assertEquals(SOUTH, config.side)
        assertEquals("routine", config.quanomous)
        assertEquals(1.0, config.delay, 0.0)
        assertEquals(0.5, config.responsiveness, 0.0)
        assertTrue(config.robotCentric)
    }

    @Test
    fun diagnosticsDefaultsAndLiveConfigurationAreShared() {
        val defaults = Config.Config()
        assertEquals(INFO, defaults.level)
        assertEquals("", defaults.filter)

        config.filter = "Gate"
        Config.initialize()

        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.INFO,
            TeamTelemetry.LEVEL)
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Level.INFO,
            Logging.LEVEL)
        assertEquals("Gate", TeamTelemetry.DISPLAY_FILTER)
        assertEquals("Gate", Logging.DISPLAY_FILTER)

        config.level = WARN
        config.filter = "Deflector"
        TeamTelemetry.beginFrame()
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.WARN,
            TeamTelemetry.LEVEL)
        assertEquals(org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Level.WARN,
            Logging.LEVEL)
        assertEquals("Deflector", TeamTelemetry.DISPLAY_FILTER)
        assertEquals("Deflector", Logging.DISPLAY_FILTER)
    }

    @Test
    fun driverControlsDefaultToRobotCentric() {
        assertTrue(Config.Config().robotCentric)
    }

    @Test
    fun periodicDisplaysEveryItem() {
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        assertEquals(
            listOf("Alliance", "Side", "Delay", "Responsiveness", "Robot Centric",
                "Level"),
            Config.items.map { it.key }
        )
        assertEquals("0.0s", Config.items[2].value())
        assertEquals("1.00", Config.items[3].value())
        assertEquals(false, Config.items[4].value())
        verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("CONFIG"))
        verify(ActiveOpMode.telemetry).addData("Alliance", UNKNOWN as Any)
    }

    @Test
    fun configurationRemainsVisibleWhenTheDisplayIsFiltered() {
        config.filter = "Gate"
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("CONFIG"))
    }

    @Test
    fun menuNavigationAndEditingRespectMatchState() {
        Config.changeItem(NEXT)
        assertEquals(0, state.setting)

        Config.edit.start()
        Config.prevItem.start()
        assertEquals(0, state.setting)
        Config.nextItem.start()
        assertEquals(1, state.setting)

        state.started = true
        Config.prevValue.start()
        assertEquals(UNKNOWN_SIDE, config.side)

        state.setting = 3
        Config.nextValue.start()
        assertEquals(1.0, config.responsiveness, 0.0)
        Config.prevValue.start()
        assertEquals(0.95, config.responsiveness, 0.0)

        Config.done.start()
        assertFalse(state.configurable)
        Config.changeValue(NEXT)
    }

    @Test
    fun everyMenuSettingCanBeChanged() {
        state.configurable = true
        Config.items.indices.forEach {
            state.setting = it
            Config.changeValue(NEXT)
        }

        assertEquals(RED, config.alliance)
        assertEquals(NORTH, config.side)
        assertEquals(0.5, config.delay, 0.0)
        assertEquals(1.0, config.responsiveness, 0.0)
        assertTrue(config.robotCentric)
        assertEquals(WARN, config.level)

        state.setting = 0
        Config.changeValue(NEXT)
        state.setting = 1
        Config.changeValue(NEXT)
        state.setting = 4
        Config.changeValue(NEXT)
        assertEquals(BLUE, config.alliance)
        assertEquals(SOUTH, config.side)
        assertFalse(config.robotCentric)
    }

    @Test
    fun changingAllianceOrSideResetsTheStartingPose() {
        val follower = dev.nextftc.extensions.pedro.PedroComponent.follower
        state.configurable = true

        state.setting = 0
        Config.changeValue(NEXT)
        verify(follower).setStartingPose(org.mockito.ArgumentMatchers.any(Pose::class.java))
        verify(follower).setPose(org.mockito.ArgumentMatchers.any(Pose::class.java))

        clearInvocations(follower)
        state.setting = 2
        Config.changeValue(NEXT)
        verify(follower, never()).setStartingPose(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun onlyActualEditableValueChangesNotifyPersistence() {
        var changes = 0
        ConfigComponent.onChange = { changes++ }
        state.configurable = true
        state.setting = 2

        Config.changeValue(NEXT)
        assertEquals(1, changes)

        state.started = true
        Config.changeValue(NEXT)
        assertEquals(1, changes)

        state.setting = 3
        config.responsiveness = 1.0
        Config.changeValue(NEXT)
        assertEquals(1, changes)
    }

    @Test
    fun captionsIdentifySelectionAndLockedSettings() {
        val alliance = Config.items.first()
        val responsiveness = Config.items[3]

        assertEquals("Alliance", Config.caption(alliance))
        state.configurable = true
        assertEquals("Responsiveness", Config.caption(responsiveness))
        assertEquals(">Alliance", Config.caption(alliance))
        state.started = true
        assertEquals("xAlliance", Config.caption(alliance))
        state.setting = 3
        assertEquals(">Responsiveness", Config.caption(responsiveness))
    }

    @Test
    fun startRecordsMatchStart() {
        Config.start()

        assertTrue(state.started)
    }

    @Test
    fun configurationStructureSeparatesPersistentAndRuntimeState() {
        assertTrue(Config::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(-1, PREV.sign)
        assertEquals(1, NEXT.sign)
        assertEquals(
            listOf("alliance", "side", "delay", "responsiveness", "robotCentric",
                "level", "filter", "quanomous"),
            Config.Config::class.java.declaredFields
                .filterNot { it.isSynthetic }
                .map { it.name }
        )
        assertEquals(
            listOf("alliance", "side", "delay", "responsiveness", "robotCentric",
                "level"),
            Config.Config::class.java.declaredFields
                .filter { it.isAnnotationPresent(Setting::class.java) }
                .map { it.name }
        )
        val configField = Config::class.java.getDeclaredField("config")
        assertTrue(Modifier.isStatic(configField.modifiers))
        assertFalse(Modifier.isFinal(configField.modifiers))
        assertEquals(ConfigSubsystem::class.java, Config::class.java.superclass)
        assertEquals("Config.edit", Config.edit.name)
        assertEquals("Config.done", Config.done.name)
        assertEquals("Config.prevItem", Config.prevItem.name)
        assertEquals("Config.nextItem", Config.nextItem.name)
        assertEquals("Config.prevValue", Config.prevValue.name)
        assertEquals("Config.nextValue", Config.nextValue.name)
    }
}
