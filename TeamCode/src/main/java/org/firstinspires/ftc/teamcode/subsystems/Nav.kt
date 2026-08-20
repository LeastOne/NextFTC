package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.units.inches
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.NavSubsystem
import org.firstinspires.ftc.teamcode.game.Alliance.UNKNOWN
import org.firstinspires.ftc.teamcode.game.Side.UNKNOWN as UNKNOWN_SIDE
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import kotlin.math.PI

object Nav : NavSubsystem() {
    override val robotLength = 14.25.inches
    override val robotWidth = 11.375.inches

    val start get() = if (config.alliance == UNKNOWN || config.side == UNKNOWN_SIDE)
        pose(0.tiles, 0.tiles)
    else
        pose((config.side.sign * 2.7).tiles, (config.alliance.sign * -0.8).tiles)
    val score = pose(1.tiles, 0.5.tiles, PI / 2)
    val park = pose(1.5.tiles, 0.tiles)
    val gateIntake get() = pose(0.65.tiles, (config.alliance.sign * -2.65).tiles,
        config.alliance.sign * -3 * PI / 4)
    val goal get() = pose((-2.75).tiles, (config.alliance.sign * -2.75).tiles,
        config.alliance.sign * PI / 4)
}
