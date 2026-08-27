package org.firstinspires.ftc.teamcode.subsystems

import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TimingTests : SubsystemTests() {
    lateinit var playTimer: ElapsedTime
    lateinit var periodicTimer: ElapsedTime

    @Before
    fun setUp() {
        TeamTelemetry.LEVEL = DEBUG
        playTimer = mock(ElapsedTime::class.java)
        periodicTimer = mock(ElapsedTime::class.java)
        Timing.playTimer = playTimer
        Timing.periodicTimer = periodicTimer
        Timing.ENDGAME_SECONDS = 75.0
        Timing.rumbled = false
    }

    @After
    fun tearDown() {
        Timing.playTimer = ElapsedTime()
        Timing.periodicTimer = ElapsedTime()
    }

    @Test
    fun initializesAndStartsTimers() {
        Timing.initialize()
        Timing.start()

        verify(periodicTimer).reset()
        verify(playTimer, org.mockito.Mockito.times(2)).reset()
    }

    @Test
    fun periodicDisplaysLoopTiming() {
        `when`(playTimer.seconds()).thenReturn(10.06)
        `when`(periodicTimer.milliseconds()).thenReturn(20.04)
        clearInvocations(ActiveOpMode.telemetry)

        Timing.periodic()

        verify(ActiveOpMode.telemetry).addLine(TeamTelemetry.title("TEL"))
        verify(ActiveOpMode.telemetry).addData("D | Timing | Runtime (s)", "10.1" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Timing | Loop (ms)", "20" as Any)
        verify(ActiveOpMode.telemetry).addData("D | Timing | Rate (Hz)", "49.9" as Any)
        verify(periodicTimer).milliseconds()
        verify(periodicTimer).reset()
    }

    @Test
    fun rumblesBothGamepadsOnceAtEndgame() {
        val gamepad1 = mock(Gamepad::class.java)
        val gamepad2 = mock(Gamepad::class.java)
        ActiveOpMode.it!!.gamepad1 = gamepad1
        ActiveOpMode.it!!.gamepad2 = gamepad2
        `when`(playTimer.seconds()).thenReturn(75.0)
        `when`(periodicTimer.milliseconds()).thenReturn(10.0)

        Timing.periodic()
        Timing.periodic()

        verify(gamepad1).rumble(1.0, 1.0, 1000)
        verify(gamepad2).rumble(1.0, 1.0, 1000)
        assertTrue(Timing.rumbled)
    }
}
