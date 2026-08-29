package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import org.firstinspires.ftc.threedrd.nextftc.hardware.ServoEx
import org.firstinspires.ftc.threedrd.nextftc.hardware.update
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

import dev.nextftc.ftc.Gamepads.gamepad2

@Configurable
object Deflector : Subsystem() {
    var MIN = 0.0
    var MAX = 0.8
    var POS = 0.5
    var INC = 0.02

    val servo = ServoEx("deflector") { scaleRange(MIN, MAX) }.apply { reversed() }

    val up by instant { move(+INC) }
    val down by instant { move(-INC) }

    override fun controls() {
        val activated = !gamepad2.start and gamepad2.y
        (activated and gamepad2.dpadUp) whenBecomesTrue up
        (activated and gamepad2.dpadDown) whenBecomesTrue down
    }

    override fun periodic() {
        servo.update { position = POS }
    }

    fun move(change: Double) {
        POS = (POS + change).coerceIn(0.0, 1.0)
    }
}
