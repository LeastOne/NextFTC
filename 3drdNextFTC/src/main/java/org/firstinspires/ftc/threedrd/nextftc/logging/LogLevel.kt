package org.firstinspires.ftc.threedrd.nextftc.logging

enum class LogLevel(val indicator: Char) {
    VERBOSE('V'),
    DEBUG('D'),
    INFO('I'),
    WARN('W'),
    ERROR('E'),
    ASSERT('A'),
    OFF('-');

    fun accepts(level: LogLevel) = this != OFF && level != OFF && level.ordinal >= ordinal
}
