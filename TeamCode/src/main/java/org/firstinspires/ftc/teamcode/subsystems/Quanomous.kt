package org.firstinspires.ftc.teamcode.subsystems

import com.google.gson.JsonObject
import dev.nextftc.core.commands.delays.Delay
import dev.nextftc.core.units.deg
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.tiles
import org.firstinspires.ftc.teamcode.adaptations.quanomous.QuanomousCompiler
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.game.Side.SOUTH
import org.firstinspires.ftc.teamcode.subsystems.Config.alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.config

object Quanomous : Subsystem() {
    val compiler by lazy { QuanomousCompiler(mapOf(
        "delay" to { step -> Delay(step["seconds"].asDouble.seconds) },
        "intake" to { step -> Auto.remaining(Auto.intake(step["spike"].asInt)) },
        "intake_gate" to { Auto.remaining(Auto.gateIntake()) },
        "deposit" to { step -> Auto.remaining(Auto.deposit(
            if (step["locale"].asString == "near") SOUTH else NORTH,
            step["txo"].asDouble.tiles,
            step["tyo"].asDouble.tiles
        )) },
        "release" to { Auto.remaining(Auto.releaseGate()) },
        "chase" to { step -> Auto.remaining(Auto.chase(step["cycles"].asInt.let {
            if (it == 0) Int.MAX_VALUE else it
        })) },
        "park" to { step -> Auto.park(
            step.boolean("gate", config.parkGate),
            step.axial(),
            step.lateral()
        ) },
        "drive" to { step -> Auto.remaining(Auto.drive(Nav.pose(
            step["tx"].asDouble.tiles,
            alliance(-abs(step["ty"].asDouble)).tiles,
            step["h"].asDouble.deg,
            step.axial(),
            step.lateral()
        ))) }
    )) }

    fun load(name: String) = compiler.load(name)

    fun JsonObject.boolean(name: String, default: Boolean = false) = get(name)?.asBoolean ?: default

    fun JsonObject.text(name: String, default: String = "center") = get(name)?.asString ?: default

    fun JsonObject.axial() = when (text("axial").lowercase()) {
        "front" -> Axial.FRONT
        "back" -> Axial.BACK
        else -> Axial.CENTER
    }

    fun JsonObject.lateral() = when (text("lateral").lowercase()) {
        "left" -> Lateral.LEFT
        "right" -> Lateral.RIGHT
        else -> Lateral.CENTER
    }
}
