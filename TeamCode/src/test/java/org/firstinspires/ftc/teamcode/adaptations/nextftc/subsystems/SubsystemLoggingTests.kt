package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.nextftc.core.subsystems.Subsystem as NextSubsystem
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LoggingComponent
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SubsystemLoggingTests {
    private object TestSubsystem : NextSubsystem

    private lateinit var telemetry: Telemetry
    private lateinit var telemetryLog: Telemetry.Log

    @Before
    fun setUp() {
        telemetry = mock(Telemetry::class.java)
        telemetryLog = mock(Telemetry.Log::class.java)
        `when`(telemetry.log()).thenReturn(telemetryLog)
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }.apply { this.telemetry = this@SubsystemLoggingTests.telemetry }

        Logging.LOG_LEVEL = LogLevel.OFF
        Logging.TELEMETRY_LEVEL = LogLevel.VERBOSE
        Logging.TELEMETRY_TAG = ""
        Logging.TELEMETRY_TEXT = ""
    }

    @After
    fun tearDown() {
        Logging.LOG_LEVEL = LogLevel.DEBUG
        Logging.TELEMETRY_LEVEL = LogLevel.VERBOSE
    }

    @Test
    fun everyConvenienceMethodDelegatesToLogging() {
        TestSubsystem.verbose("Value", 1)
        TestSubsystem.debug("Value", 1)
        TestSubsystem.info("Value", 1)
        TestSubsystem.warn("Value", 1)
        TestSubsystem.error("Value", 1)
        TestSubsystem.fatal("Value", 1)

        TestSubsystem.verbose("Lazy") { 1 }
        TestSubsystem.debug("Lazy") { 1 }
        TestSubsystem.info("Lazy") { 1 }
        TestSubsystem.warn("Lazy") { 1 }
        TestSubsystem.error("Lazy") { 1 }
        TestSubsystem.fatal("Lazy") { 1 }

        TestSubsystem.verbose("Message")
        TestSubsystem.debug("Message")
        TestSubsystem.info("Message")
        TestSubsystem.warn("Message")
        TestSubsystem.error("Message")
        TestSubsystem.fatal("Message")

        TestSubsystem.verbose { "Lazy message" }
        TestSubsystem.debug { "Lazy message" }
        TestSubsystem.info { "Lazy message" }
        TestSubsystem.warn { "Lazy message" }
        TestSubsystem.error { "Lazy message" }
        TestSubsystem.fatal { "Lazy message" }

        verify(telemetry, times(6)).addData("TestSubsystem (Value)", 1 as Any)
        verify(telemetry, times(6)).addData("TestSubsystem (Lazy)", 1 as Any)
        verify(telemetryLog, times(12)).add(anyString())
    }
}
