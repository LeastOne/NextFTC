package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Level as LogLevel
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level as TelLevel

interface DiagnosticsConfig {
    val level: Level
    val filter: String

    enum class Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, OFF
    }
}

fun DiagnosticsConfig.Level.toLogLevel() = LogLevel.valueOf(name)
fun DiagnosticsConfig.Level.toTelLevel() = TelLevel.valueOf(name)
