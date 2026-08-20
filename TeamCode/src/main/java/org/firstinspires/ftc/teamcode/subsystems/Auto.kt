package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import org.firstinspires.ftc.teamcode.adaptations.nextftc.commands.alongWith
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.pct
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.resetStartingPose
import org.firstinspires.ftc.teamcode.subsystems.Config.config

object Auto : Subsystem() {
    val locate by instant { follower.resetStartingPose(Nav.start) }

    fun execute() = locate
        .thenWait(config.delay)
        .then(
            Gate.close,
            Drive.toScore.alongWith(
                Drive.until(50.pct).then(Deflector.up)
            ),
            Gate.open.thenWait(0.5).then(Gate.close),
            Drive.toPark.alongWith(
                Drive.until(25.pct).then(Deflector.down)
            )
        )
}
