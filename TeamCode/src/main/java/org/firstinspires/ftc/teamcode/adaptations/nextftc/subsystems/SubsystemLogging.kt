package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import dev.nextftc.core.subsystems.Subsystem as NextSubsystem
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging.log
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging.telemetry
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.WARN
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.ERROR
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.ASSERT

fun NextSubsystem.telemetry(level: LogLevel, caption: String, value: Any?) = telemetry(this, level, caption, value)
fun NextSubsystem.verbose(caption: String, value: Any?) = log(this, VERBOSE, caption, value)
fun NextSubsystem.debug(caption: String, value: Any?) = log(this, DEBUG, caption, value)
fun NextSubsystem.info(caption: String, value: Any?) = log(this, INFO, caption, value)
fun NextSubsystem.warn(caption: String, value: Any?) = log(this, WARN, caption, value)
fun NextSubsystem.error(caption: String, value: Any?) = log(this, ERROR, caption, value)
fun NextSubsystem.fatal(caption: String, value: Any?) = log(this, ASSERT, caption, value)
fun NextSubsystem.verbose(caption: String, value: () -> Any?) = log(this, VERBOSE, caption, value)
fun NextSubsystem.debug(caption: String, value: () -> Any?) = log(this, DEBUG, caption, value)
fun NextSubsystem.info(caption: String, value: () -> Any?) = log(this, INFO, caption, value)
fun NextSubsystem.warn(caption: String, value: () -> Any?) = log(this, WARN, caption, value)
fun NextSubsystem.error(caption: String, value: () -> Any?) = log(this, ERROR, caption, value)
fun NextSubsystem.fatal(caption: String, value: () -> Any?) = log(this, ASSERT, caption, value)
fun NextSubsystem.verbose(message: String) = log(this, VERBOSE, null, message)
fun NextSubsystem.debug(message: String) = log(this, DEBUG, null, message)
fun NextSubsystem.info(message: String) = log(this, INFO, null, message)
fun NextSubsystem.warn(message: String) = log(this, WARN, null, message)
fun NextSubsystem.error(message: String) = log(this, ERROR, null, message)
fun NextSubsystem.fatal(message: String) = log(this, ASSERT, null, message)
fun NextSubsystem.verbose(message: () -> String) = log(this, VERBOSE, null, message)
fun NextSubsystem.debug(message: () -> String) = log(this, DEBUG, null, message)
fun NextSubsystem.info(message: () -> String) = log(this, INFO, null, message)
fun NextSubsystem.warn(message: () -> String) = log(this, WARN, null, message)
fun NextSubsystem.error(message: () -> String) = log(this, ERROR, null, message)
fun NextSubsystem.fatal(message: () -> String) = log(this, ASSERT, null, message)
