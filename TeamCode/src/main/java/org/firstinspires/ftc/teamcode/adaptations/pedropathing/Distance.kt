package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import dev.nextftc.core.units.inches

val TILE_WIDTH = 23.5.inches

val Number.tiles get() = TILE_WIDTH * toDouble()
val Number.tile get() = tiles
