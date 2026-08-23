package org.firstinspires.ftc.threedrd.nextftc.config

import java.lang.reflect.Field
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel

class Diagnostics(val config: Any? = null) {
    enum class Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, OFF
    }

    val levelField = field("level")
    val filterField = field("filter")

    fun level() = levelField?.get(config) as? Level ?: OFF
    fun filter() = filterField?.get(config) as? String ?: ""

    fun field(name: String): Field? = generateSequence(config?.javaClass) { it.superclass }
        .flatMap { it.declaredFields.asSequence() }
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
}

fun Diagnostics.Level.toLogLevel() = LogLevel.valueOf(name)
fun Diagnostics.Level.toTelLevel() = TelemetryLevel.valueOf(name)
