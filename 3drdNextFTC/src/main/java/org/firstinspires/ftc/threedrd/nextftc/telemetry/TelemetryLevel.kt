package org.firstinspires.ftc.threedrd.nextftc.telemetry

enum class TelemetryLevel(val indicator: Char) {
    VERBOSE('V'),
    DEBUG('D'),
    INFO('I'),
    WARN('W'),
    ERROR('E'),
    ASSERT('A'),
    OFF('-');

    fun accepts(level: TelemetryLevel) = this != OFF && level != OFF && level.ordinal >= ordinal
}
