package org.firstinspires.ftc.threedrd.nextftc.logging

import com.bylazar.configurables.annotations.Configurable
import com.bylazar.configurables.annotations.IgnoreConfigurable
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.ftc.ActiveOpMode
import com.qualcomm.robotcore.util.RobotLog.aa
import com.qualcomm.robotcore.util.RobotLog.dd
import com.qualcomm.robotcore.util.RobotLog.ee
import com.qualcomm.robotcore.util.RobotLog.ii
import com.qualcomm.robotcore.util.RobotLog.vv
import com.qualcomm.robotcore.util.RobotLog.ww
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.ASSERT
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.ERROR
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.OFF
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.WARN
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics
import org.firstinspires.ftc.threedrd.nextftc.config.toLogLevel

@Configurable
object Logging {
    var LEVEL = OFF
    var FILTER = ""
    @field:IgnoreConfigurable
    var DISPLAY_FILTER = ""
    @field:IgnoreConfigurable
    var commandSnapshot = emptyList<String>()
    @field:IgnoreConfigurable
    var displayedFilter = ""
    @field:IgnoreConfigurable
    var displayedLevel = LEVEL
    @field:IgnoreConfigurable
    var displayedSpecificFilter = ""
    @field:IgnoreConfigurable
    val history = mutableListOf<LogEntry>()
    @field:IgnoreConfigurable
    private var diagnostics: Diagnostics? = null
    @field:IgnoreConfigurable
    private var configuredLevel = OFF

    fun initialize() {
        diagnostics = null
        bind(null)
        commandSnapshot = emptyList()
        history.clear()
        displayedFilter = DISPLAY_FILTER
        displayedLevel = LEVEL
        displayedSpecificFilter = FILTER
    }

    fun beginFrame() {
        sampleDiagnostics()
        logCommandSnapshotChanges()
        if (LEVEL != displayedLevel || DISPLAY_FILTER != displayedFilter ||
            FILTER != displayedSpecificFilter) rebuild()
        displayedFilter = DISPLAY_FILTER
        displayedLevel = LEVEL
        displayedSpecificFilter = FILTER
    }

    fun bind(config: Diagnostics?) {
        diagnostics = config
        configuredLevel = config.level()
        LEVEL = configuredLevel
        DISPLAY_FILTER = config.filter()
    }

    fun sampleDiagnostics() {
        val config = diagnostics
        val nextLevel = config.level()
        if (LEVEL == configuredLevel) LEVEL = nextLevel
        configuredLevel = nextLevel
        DISPLAY_FILTER = config.filter()
    }

    fun Diagnostics?.level() = if (this == null) OFF else level().toLogLevel()
    fun Diagnostics?.filter() = if (this == null) "" else filter()

    fun add(tag: String, level: LogLevel, message: String, context: String = "") {
        if (level == OFF) return
        writeToRobotLog(level, tag, message)
        val entry = LogEntry(level, tag, message, context)
        retain(entry)
        if (visible(entry)) ActiveOpMode.telemetry.log().add(entry.line)
    }

    fun add(tag: String, level: LogLevel, message: () -> String) = add(tag, level, message())

    fun writeToRobotLog(level: LogLevel, tag: String, message: String) {
        when (level) {
            VERBOSE -> vv(tag, message)
            DEBUG -> dd(tag, message)
            INFO -> ii(tag, message)
            WARN -> ww(tag, message)
            ERROR -> ee(tag, message)
            ASSERT -> aa(tag, message)
            OFF -> Unit
        }
    }

    fun logCommandSnapshotChanges() {
        val current = CommandManager.snapshot.filterNot { it == "NullCommand" }
        if (current == commandSnapshot) return

        val message = if (current.isEmpty()) "Idle" else "Running | ${current.joinToString()}"
        add("Commands", DEBUG, message, commandSnapshot.joinToString())
        commandSnapshot = current
    }

    fun retain(entry: LogEntry) {
        history += entry
        while (history.size > ActiveOpMode.telemetry.log().capacity) history.removeAt(0)
    }

    fun visible(entry: LogEntry) = LEVEL.accepts(entry.level) &&
        entry.matches(DISPLAY_FILTER) && entry.matches(FILTER)

    fun visibleEntries() = history.filter(::visible)

    fun rebuild() {
        ActiveOpMode.telemetry.log().clear()
        visibleEntries().forEach { ActiveOpMode.telemetry.log().add(it.line) }
    }
}

class Logger(val tag: String) {
    fun add(level: LogLevel, message: String) = Logging.add(tag, level, message)
    fun verbose(message: String) = add(VERBOSE, message)
    fun debug(message: String) = add(DEBUG, message)
    fun info(message: String) = add(INFO, message)
    fun warn(message: String) = add(WARN, message)
    fun error(message: String) = add(ERROR, message)
    fun fatal(message: String) = add(ASSERT, message)

    fun add(level: LogLevel, message: () -> String) = Logging.add(tag, level, message)
    fun verbose(message: () -> String) = add(VERBOSE, message)
    fun debug(message: () -> String) = add(DEBUG, message)
    fun info(message: () -> String) = add(INFO, message)
    fun warn(message: () -> String) = add(WARN, message)
    fun error(message: () -> String) = add(ERROR, message)
    fun fatal(message: () -> String) = add(ASSERT, message)
}
