package org.firstinspires.ftc.threedrd.nextftc.telemetry

import com.bylazar.telemetry.PanelsTelemetry
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.RobotLog
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.LambdaCommand
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TelemetryComponentTests {
    private lateinit var telemetry: Telemetry
    private lateinit var telemetryLog: Telemetry.Log
    private lateinit var robotLog: MockedStatic<RobotLog>

    @Before
    fun setUp() {
        CommandManager.cancelAll()
        CommandManager.run()
        telemetry = mock(Telemetry::class.java)
        telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        `when`(telemetryLog.capacity).thenReturn(9)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = this@TelemetryComponentTests.telemetry }
        Logging.DISPLAY_FILTER = ""
        Logging.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        TeamTelemetry.FILTER = ""
        PanelsTelemetry.telemetry.lines = mutableListOf()
        robotLog = mockStatic(RobotLog::class.java)
        TelemetryComponent.preInit()
        TeamTelemetry.output = telemetry
        Logging.LEVEL = DEBUG
        TeamTelemetry.LEVEL = org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO
        Logging.beginFrame()
        clearInvocations(telemetryLog)
    }

    @After
    fun tearDown() {
        robotLog.close()
        CommandManager.cancelAll()
        CommandManager.run()
    }

    @Test
    fun componentRebuildsAndUpdatesBothLoopPhases() {
        TelemetryComponent.preWaitForStart()
        TelemetryComponent.preUpdate()
        TelemetryComponent.postWaitForStart()
        TelemetryComponent.postUpdate()

        verify(telemetry, times(2)).clear()
        verify(telemetry, times(2)).update()
    }

    @Test
    fun visibleHistoryGetsATitledLogSection() {
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetry)

        TelemetryComponent.postUpdate()

        verify(telemetry).addLine(TeamTelemetry.title("LOG"))
        verify(telemetry).update()
    }

    @Test
    fun commonAndSpecificFiltersRebuildHistory() {
        Logging.add("Gate", INFO, "Opened")
        Logging.add("Gate", INFO, "Closed")
        Logging.add("Deflector", INFO, "Up")
        clearInvocations(telemetryLog)
        Logging.DISPLAY_FILTER = "Gate"
        Logging.FILTER = "Open"

        TelemetryComponent.preUpdate()

        verify(telemetryLog).clear()
        verify(telemetryLog).add("I | Gate | Opened")
        verify(telemetryLog, never()).add("I | Gate | Closed")
        verify(telemetryLog, never()).add("I | Deflector | Up")
    }

    @Test
    fun changingOnlyTheSpecificFilterRebuildsHistoryAndHidesNewMismatches() {
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetryLog)
        Logging.FILTER = "Deflector"

        TelemetryComponent.preUpdate()
        Logging.add("Gate", INFO, "Closed")

        verify(telemetryLog).clear()
        verify(telemetryLog, never()).add("I | Gate | Opened")
        verify(telemetryLog, never()).add("I | Gate | Closed")
    }

    @Test
    fun levelChangesRebuildHistory() {
        Logging.LEVEL = VERBOSE
        TelemetryComponent.preUpdate()
        Logging.add("Gate", VERBOSE, "Position")
        Logging.add("Gate", INFO, "Opened")
        clearInvocations(telemetryLog)
        Logging.LEVEL = INFO

        TelemetryComponent.preUpdate()

        verify(telemetryLog).clear()
        verify(telemetryLog, never()).add("V | Gate | Position")
        verify(telemetryLog).add("I | Gate | Opened")
    }

    @Test
    fun historyUsesTheDriverStationCapacity() {
        `when`(telemetryLog.capacity).thenReturn(2)
        Logging.add("Test", INFO, "Event 1")
        Logging.add("Test", INFO, "Event 2")
        Logging.add("Test", INFO, "Event 3")
        clearInvocations(telemetryLog)

        Logging.rebuild()

        verify(telemetryLog, never()).add("I | Test | Event 1")
        verify(telemetryLog).add("I | Test | Event 2")
        verify(telemetryLog).add("I | Test | Event 3")
    }

    @Test
    fun commandSnapshotsLogChangesAndAssociateIdleWithThePreviousCommand() {
        val drive = LambdaCommand("Drive").setIsDone { false }
        CommandManager.scheduleCommand(drive)
        CommandManager.run()

        TelemetryComponent.preUpdate()
        TelemetryComponent.preUpdate()
        CommandManager.cancelCommand(drive)
        CommandManager.run()
        TelemetryComponent.preUpdate()

        verify(telemetryLog).add("D | Commands | Running | Drive")
        verify(telemetryLog).add("D | Commands | Idle")
        val idle = Logging.history.last()
        org.junit.Assert.assertTrue(idle.matches("Drive"))
    }

    @Test
    fun commandSnapshotsExcludeNullCommands() {
        val nullCommand = LambdaCommand("NullCommand").setIsDone { false }
        CommandManager.scheduleCommand(nullCommand)
        CommandManager.run()

        TelemetryComponent.preUpdate()

        verify(telemetryLog, never()).add("D | Commands | Running | NullCommand")
    }
}
