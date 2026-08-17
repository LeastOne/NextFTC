package org.firstinspires.ftc.teamcode.adaptations.nextftc.logging

import com.qualcomm.robotcore.util.RobotLog

import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.WARN
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.ERROR
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.ASSERT
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.OFF

import dev.nextftc.core.components.Component
import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.subsystems.Subsystem
import dev.nextftc.ftc.ActiveOpMode.telemetry

object Logging : Component {
    var LOG_LEVEL = DEBUG

    var TELEMETRY_LEVEL = VERBOSE
    var TELEMETRY_TAG = ""
    var TELEMETRY_TEXT = ""

    private var commandSnapshot = emptyList<String>()

    fun log(owner: Subsystem, level: LogLevel, caption: String?, value: Any?) =
        log(owner.javaClass.simpleName, level, caption, value)

    fun log(tag: String, level: LogLevel, caption: String?, value: Any?) {
        val message = if (caption == null) value.toString() else "$caption | $value"
        val searchableText = if (caption == null) message else "$caption $value"

        if (LOG_LEVEL.accepts(level)) writeToRobotLog(level, tag, message)
        if (TELEMETRY_LEVEL.accepts(level) &&
            tag.matches(TELEMETRY_TAG) &&
            searchableText.matches(TELEMETRY_TEXT)
        ) {
            when (caption) {
                null -> telemetry.log().add("$level $tag | $message")
                else -> telemetry.addData("$tag ($caption)", value)
            }
        }
    }

    fun log(owner: Subsystem, level: LogLevel, caption: String?, valueProvider: () -> Any?) =
        log(owner.javaClass.simpleName, level, caption, valueProvider)

    fun log(tag: String, level: LogLevel, caption: String?, valueProvider: () -> Any?) {
        if (LOG_LEVEL.accepts(level) ||
            TELEMETRY_LEVEL.accepts(level) &&
            tag.matches(TELEMETRY_TAG)
        ) log(tag, level, caption, valueProvider())
    }

    override fun preInit() {
        commandSnapshot = emptyList()
    }

    override fun preWaitForStart() {
        logCommandSnapshotChanges()
    }

    override fun preUpdate() {
        logCommandSnapshotChanges()
    }

    override fun postWaitForStart() {
        telemetry.update()
    }

    override fun postUpdate() {
        telemetry.update()
    }

    fun logCommandSnapshotChanges() {
        val current = CommandManager.snapshot.filterNot { it == "NullCommand" }
        if (current == commandSnapshot) return

        val state = if (current.isEmpty()) "Idle" else "Running | ${current.joinToString()}"
        log("Commands", DEBUG, null, state)
        commandSnapshot = current
    }

    fun LogLevel.accepts(level: LogLevel) =
        this != OFF && level != OFF && level.ordinal >= ordinal

    fun String.matches(filter: String) = contains(filter, ignoreCase = true)

    fun writeToRobotLog(level: LogLevel, tag: String, message: String) {
        when (level) {
            VERBOSE -> RobotLog.vv(tag, message)
            DEBUG -> RobotLog.dd(tag, message)
            INFO -> RobotLog.ii(tag, message)
            WARN -> RobotLog.ww(tag, message)
            ERROR -> RobotLog.ee(tag, message)
            ASSERT -> RobotLog.aa(tag, message)
            OFF -> Unit
        }
    }
}
