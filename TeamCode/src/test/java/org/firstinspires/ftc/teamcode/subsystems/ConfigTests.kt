package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.threedrd.nextftc.config.Setting
import org.firstinspires.ftc.threedrd.nextftc.config.ConfigComponent
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.junit.Assert.*
import org.junit.*
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.WARN
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.NEXT
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.PREV
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.SOUTH
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.side
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class ConfigTests : SubsystemTests() {
    lateinit var pedro: PedroComponent

    @Before
    fun resetConfig() {
        Config.alliance = UNKNOWN
        Config.side = UNKNOWN_SIDE
        Config.quanomous = null
        Config.delay = 0.0
        Config.responsiveness = 1.0
        Config.robotCentric = false
        Config.parkGate = false
        Config.goalDistanceOffsetSouth = 0.0
        Config.goalDistanceOffsetNorth = 0.0
        Config.goalAngleOffsetSouth = 0.0
        Config.goalAngleOffsetNorth = 0.0
        Config.level = INFO
        Config.filter = ""
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
        Config.alliance = BLUE
        Config.side = SOUTH
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
        assertEquals(BLUE, alliance)
        assertEquals(SOUTH, side)
    }

    @Test
    fun exposesCurrentAllianceAndSide() {
        Config.alliance = BLUE
        Config.side = SOUTH

        assertEquals(BLUE, alliance)
        assertEquals(SOUTH, side)
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
    fun initializesAutonomousStateWithFreshFieldSelection() {
        ActiveOpMode.it = TestAuto()
        Config.alliance = BLUE
        Config.side = SOUTH
        Config.quanomous = "routine"

        Config.initialize()

        assertTrue(state.auto)
        assertFalse(state.teleop)
        assertFalse(state.interrupt)
        assertEquals(UNKNOWN, alliance)
        assertEquals(UNKNOWN_SIDE, side)
        assertEquals(null, Config.quanomous)
    }

    @Test
    fun configurationAndRuntimeStateCanBeUpdatedDirectly() {
        Config.alliance = BLUE
        Config.side = SOUTH
        Config.quanomous = "routine"
        Config.delay = 1.0
        Config.responsiveness = 0.5
        Config.robotCentric = true
        Config.parkGate = true
        Config.goalDistanceOffsetSouth = 1.0
        Config.goalDistanceOffsetNorth = 2.0
        Config.goalAngleOffsetSouth = 3.0
        Config.goalAngleOffsetNorth = 4.0

        assertEquals(BLUE, Config.alliance)
        assertEquals(SOUTH, Config.side)
        assertEquals("routine", Config.quanomous)
        assertEquals(1.0, Config.delay, 0.0)
        assertEquals(0.5, Config.responsiveness, 0.0)
        assertTrue(Config.robotCentric)
        assertTrue(Config.parkGate)
        assertEquals(1.0, Config.goalDistanceOffsetSouth, 0.0)
        assertEquals(2.0, Config.goalDistanceOffsetNorth, 0.0)
        assertEquals(3.0, Config.goalAngleOffsetSouth, 0.0)
        assertEquals(4.0, Config.goalAngleOffsetNorth, 0.0)
    }

    @Test
    fun diagnosticsDefaultsAndLiveConfigurationAreShared() {
        val diagnostics = Diagnostics(Config)

        assertNotNull(diagnostics.levelField)
        assertNotNull(diagnostics.filterField)
        assertEquals(INFO, Config.level)
        assertEquals("", Config.filter)

        Config.filter = "Gate"
        Config.initialize()

        assertEquals(org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO,
            TeamTelemetry.LEVEL)
        assertEquals(org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.INFO,
            Logging.LEVEL)
        assertEquals("Gate", TeamTelemetry.DISPLAY_FILTER)
        assertEquals("Gate", Logging.DISPLAY_FILTER)

        Config.level = WARN
        Config.filter = "Deflector"
        TeamTelemetry.beginFrame()
        assertEquals(org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.WARN,
            TeamTelemetry.LEVEL)
        assertEquals(org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.WARN,
            Logging.LEVEL)
        assertEquals("Deflector", TeamTelemetry.DISPLAY_FILTER)
        assertEquals("Deflector", Logging.DISPLAY_FILTER)
    }

    @Test
    fun periodicDisplaysEveryItem() {
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        assertEquals(
            listOf("Alliance", "Side", "Quanomous", "Delay", "Responsiveness",
                "Robot Centric", "Park Gate", "Goal Distance Offset South",
                "Goal Distance Offset North", "Goal Angle Offset South",
                "Goal Angle Offset North", "Level"),
            Config.items.map { it.key }
        )
        assertEquals("0.0s", Config.items[3].value())
        assertEquals("1.00", Config.items[4].value())
        assertEquals(false, Config.items[5].value())
        assertEquals(false, Config.items[6].value())
        assertEquals("0.0 in", Config.items[7].value())
        assertEquals("0.0 deg", Config.items[9].value())
        verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("CONFIG"))
        verify(ActiveOpMode.telemetry).addData("Alliance", UNKNOWN as Any)
    }

    @Test
    fun configurationRemainsVisibleWhenTheDisplayIsFiltered() {
        Config.filter = "Gate"
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("CONFIG"))
    }

    @Test
    fun autonomousWarningAppearsAboveConfigUntilRequiredSettingsAreSelected() {
        state.auto = true
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        inOrder(ActiveOpMode.telemetry).apply {
            verify(ActiveOpMode.telemetry).addLine(
                "<b><font color='#FFC107'>WARNING: Select Alliance, Side, Quanomous before starting Auto</font></b>")
            verify(ActiveOpMode.telemetry).addLine()
            verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("CONFIG"))
        }

        Config.alliance = RED
        Config.side = NORTH
        Config.quanomous = "routine"
        clearInvocations(ActiveOpMode.telemetry)

        Config.periodic()

        verify(ActiveOpMode.telemetry, never()).addLine(
            "<b><font color='#FFC107'>WARNING: Select Alliance, Side, Quanomous before starting Auto</font></b>")
    }

    @Test
    fun autonomousReadinessRejectsBlankQuanomousSelections() {
        Config.alliance = RED
        Config.side = NORTH

        Config.quanomous = ""
        assertEquals(listOf("Quanomous"), Config.missingAutoSettings())

        Config.quanomous = " "
        assertEquals(listOf("Quanomous"), Config.missingAutoSettings())

        Config.quanomous = "routine"
        assertTrue(Config.missingAutoSettings().isEmpty())
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
        assertEquals(UNKNOWN_SIDE, Config.side)

        state.setting = 4
        Config.nextValue.start()
        assertEquals(1.0, Config.responsiveness, 0.0)
        Config.prevValue.start()
        assertEquals(0.95, Config.responsiveness, 0.0)

        state.setting = 6
        Config.nextValue.start()
        assertFalse(Config.parkGate)
        state.setting = 7
        Config.nextValue.start()
        assertEquals(6.0, Config.goalDistanceOffsetSouth, 0.0)

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

        assertEquals(RED, Config.alliance)
        assertEquals(NORTH, Config.side)
        assertEquals(0.5, Config.delay, 0.0)
        assertEquals(1.0, Config.responsiveness, 0.0)
        assertTrue(Config.robotCentric)
        assertTrue(Config.parkGate)
        assertEquals(6.0, Config.goalDistanceOffsetSouth, 0.0)
        assertEquals(6.0, Config.goalDistanceOffsetNorth, 0.0)
        assertEquals(1.0, Config.goalAngleOffsetSouth, 0.0)
        assertEquals(1.0, Config.goalAngleOffsetNorth, 0.0)
        assertEquals(WARN, Config.level)

        state.setting = 0
        Config.changeValue(NEXT)
        state.setting = 1
        Config.changeValue(NEXT)
        state.setting = 5
        Config.changeValue(NEXT)
        assertEquals(BLUE, Config.alliance)
        assertEquals(SOUTH, Config.side)
        assertFalse(Config.robotCentric)
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
        state.setting = 3
        Config.changeValue(NEXT)
        verify(follower, never()).setStartingPose(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun onlyActualEditableValueChangesNotifyPersistence() {
        var changes = 0
        ConfigComponent.onChange = { changes++ }
        state.configurable = true
        state.setting = 3

        Config.changeValue(NEXT)
        assertEquals(1, changes)

        state.started = true
        Config.changeValue(NEXT)
        assertEquals(1, changes)

        state.setting = 4
        Config.responsiveness = 1.0
        Config.changeValue(NEXT)
        assertEquals(1, changes)
    }

    @Test
    fun captionsIdentifySelectionAndLockedSettings() {
        val alliance = Config.items.first()
        val responsiveness = Config.items[4]

        assertEquals("Alliance", Config.caption(alliance))
        state.configurable = true
        assertEquals("Responsiveness", Config.caption(responsiveness))
        assertEquals(">Alliance", Config.caption(alliance))
        state.started = true
        assertEquals("xAlliance", Config.caption(alliance))
        state.setting = 4
        assertEquals(">Responsiveness", Config.caption(responsiveness))
    }

    @Test
    fun startRecordsMatchStart() {
        Config.start()

        assertTrue(state.started)
    }

    @Test
    fun autonomousRequiresAllianceSideAndQuanomousBeforeStarting() {
        ActiveOpMode.it = TestAuto()
        Config.initialize()

        val missingBoth = assertThrows(IllegalStateException::class.java) {
            Config.start()
        }
        assertEquals("Select Alliance, Side, Quanomous before starting Auto",
            missingBoth.message)
        assertFalse(state.started)

        Config.alliance = RED
        assertThrows(IllegalStateException::class.java) { Config.start() }
        assertFalse(state.started)

        Config.side = NORTH
        assertThrows(IllegalStateException::class.java) { Config.start() }
        assertFalse(state.started)

        Config.quanomous = "routine"
        Config.start()
        assertTrue(state.started)
    }

    @Test
    fun configurationStructureSeparatesPersistentAndRuntimeState() {
        assertTrue(Config::class.java.isAnnotationPresent(Configurable::class.java))
        assertEquals(-1, PREV.sign)
        assertEquals(1, NEXT.sign)
        assertEquals(
            listOf("alliance", "side", "quanomous", "delay", "responsiveness", "robotCentric",
                "parkGate", "goalDistanceOffsetSouth", "goalDistanceOffsetNorth",
                "goalAngleOffsetSouth", "goalAngleOffsetNorth", "level", "filter"),
            Config::class.java.declaredFields
                .filterNot { it.isSynthetic }
                .filterNot { it.name == "INSTANCE" || it.name == "state" || it.name == "items" }
                .map { it.name }
        )
        assertEquals(
            listOf("alliance", "side", "quanomous", "delay", "responsiveness", "robotCentric",
                "parkGate", "goalDistanceOffsetSouth", "goalDistanceOffsetNorth",
                "goalAngleOffsetSouth", "goalAngleOffsetNorth", "level"),
            Config::class.java.declaredFields
                .filter { it.isAnnotationPresent(Setting::class.java) }
                .map { it.name }
        )
        assertEquals(Config, Config.config)
        assertEquals(ConfigSubsystem::class.java, Config::class.java.superclass)
        assertEquals("Config.edit", Config.edit.name)
        assertEquals("Config.done", Config.done.name)
        assertEquals("Config.prevItem", Config.prevItem.name)
        assertEquals("Config.nextItem", Config.nextItem.name)
        assertEquals("Config.prevValue", Config.prevValue.name)
        assertEquals("Config.nextValue", Config.nextValue.name)
    }
}
