package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.ServoEx
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.update
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem

import dev.nextftc.ftc.Gamepads.gamepad2

@Configurable
object Gate : Subsystem() {
    var MIN = 0.0
    var MAX = 0.5
    var CLOSE = 0.0
    var HOLD = 0.5
    var OPEN = 1.0
    var POS = CLOSE

    val servo = ServoEx("gate") { scaleRange(MIN, MAX) }.apply { reversed() }

    val open by instant { POS = OPEN }
    val hold by instant { POS = HOLD }
    val close by instant { POS = CLOSE }

    override fun controls() {
        val activated = !gamepad2.start and gamepad2.back
        (activated and gamepad2.dpadUp) whenBecomesTrue open
        (activated and gamepad2.dpadDown) whenBecomesTrue close
    }

    override fun periodic() {
        servo.update { position = POS }
    }
}
