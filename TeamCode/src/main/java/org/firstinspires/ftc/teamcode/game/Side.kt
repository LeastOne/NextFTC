package org.firstinspires.ftc.teamcode.game

import kotlin.Double.Companion.NaN

enum class Side(val sign: Double) {
    UNKNOWN(NaN),
    NORTH(+1.0),
    SOUTH(-1.0)
}
