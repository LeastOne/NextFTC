package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.core.commands.delays.Delay
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import org.firstinspires.ftc.teamcode.adaptations.nextftc.commands.alongWith
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.pct
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.adaptations.quanomous.QuanomousProgram
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.resetStartingPose
import org.firstinspires.ftc.teamcode.subsystems.Config.config

object Auto : Subsystem() {
    val programs by lazy { QuanomousProgram(mapOf(
        "delay" to { step -> Delay(step["seconds"].asDouble.seconds) },
        "intake" to { Intake.forward.then(Intake.untilElement(), Intake.hold) },
        "intake_gate" to { Intake.forward.alongWith(Drive.to(Nav.gateIntake)) },
        "deposit" to { Flywheel.launch.then(Flywheel.untilReady(), Conveyor.launch) },
        "release" to { Gate.open },
        "chase" to { Intake.forward },
        "park" to { Drive.toPark },
        "drive" to { step ->
            Drive.to(Nav.pose(
                step["tx"].asDouble.tiles,
                (abs(step["ty"].asDouble) * -config.alliance.sign).tiles,
                Math.toRadians(step["h"].asDouble)
            ))
        },
        "score" to { Drive.toScore },
        "gate" to { step -> if (step["open"].asBoolean) Gate.open else Gate.close },
        "deflector" to { step -> if (step["up"].asBoolean) Deflector.up else Deflector.down }
    )) }

    val locate by instant { follower.resetStartingPose(Nav.start) }

    fun sample() = Gate.close.then(
            Drive.toScore.alongWith(
                Drive.until(50.pct).then(Deflector.up)
            ),
            Gate.open.thenWait(0.5).then(Gate.close),
            Drive.toPark.alongWith(
                Drive.until(25.pct).then(Deflector.down)
            )
        )

    fun selected() = if (config.quanomous == null) sample() else programs.load(config.quanomous!!)

    fun stopAll() = Drive.stop.alongWith(
        Intake.stop,
        Conveyor.stop,
        Flywheel.stop
    )

    fun execute() = locate
        .thenWait(config.delay)
        .then(selected())
        .endAfter(29.5)
        .then(stopAll())
}
