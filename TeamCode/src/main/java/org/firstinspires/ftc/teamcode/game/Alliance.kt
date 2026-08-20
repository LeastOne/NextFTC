package org.firstinspires.ftc.teamcode.game

import kotlin.Double.Companion.NaN

enum class Alliance(val sign: Double) {
    UNKNOWN(NaN),
    RED(-1.0),
    BLUE(+1.0);

    operator fun invoke(value: Number) = value.toDouble() * sign
}
