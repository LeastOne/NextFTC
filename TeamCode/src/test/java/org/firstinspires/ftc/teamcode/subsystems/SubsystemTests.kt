package org.firstinspires.ftc.teamcode.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.mockito.Answers.RETURNS_DEFAULTS
import org.mockito.Mockito.mock

abstract class SubsystemTests {
    init {
        resetTestOpMode()
        Logging.FILTER = ""
        Logging.DISPLAY_FILTER = ""
        TeamTelemetry.FILTER = ""
        TeamTelemetry.DISPLAY_FILTER = ""
        TeamTelemetry.initialize()
        Logging.LEVEL = OFF
        TeamTelemetry.LEVEL = INFO
    }

    companion object {
        private val testHardwareMap: HardwareMap
        private val testTelemetry: Telemetry

        init {
            val devices = mutableMapOf<Pair<Class<*>, String>, Any>()
            val hardwareMap = mock(HardwareMap::class.java) { invocation ->
                val type = invocation.arguments.firstOrNull() as? Class<*>
                val name = invocation.arguments.getOrNull(1) as? String

                if (invocation.method.name == "get" && type != null && name != null) {
                    devices.getOrPut(type to name) { mockDevice(type) }
                } else {
                    RETURNS_DEFAULTS.answer(invocation)
                }
            }
            val telemetry = mock(Telemetry::class.java)
            val telemetryLog = mock(Telemetry.Log::class.java)
            org.mockito.Mockito.`when`(telemetry.log()).thenReturn(telemetryLog)
            org.mockito.Mockito.`when`(telemetryLog.capacity).thenReturn(9)
            testHardwareMap = hardwareMap
            testTelemetry = telemetry

            ActiveOpMode.it = object : LinearOpMode() {
                override fun runOpMode() = Unit
            }.apply {
                this.hardwareMap = hardwareMap
                this.telemetry = telemetry
            }
        }

        private fun resetTestOpMode() {
            ActiveOpMode.it = object : LinearOpMode() {
                override fun runOpMode() = Unit
            }.apply {
                hardwareMap = testHardwareMap
                telemetry = testTelemetry
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun mockDevice(type: Class<*>): Any = mock(type as Class<Any>) { invocation ->
            invocation.method.returnType.enumConstants?.firstOrNull()
                ?: RETURNS_DEFAULTS.answer(invocation)
        }
    }
}
