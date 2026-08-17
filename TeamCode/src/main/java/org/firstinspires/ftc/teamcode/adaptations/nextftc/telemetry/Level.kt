package org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry

enum class Level(val indicator: Char) {
    VERBOSE('V'),
    DEBUG('D'),
    INFO('I'),
    WARN('W'),
    ERROR('E'),
    ASSERT('A'),
    OFF('-');

    fun accepts(level: Level) = this != OFF && level != OFF && level.ordinal >= ordinal
}
