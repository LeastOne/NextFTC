package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import dev.nextftc.ftc.Gamepads.gamepad1
import dev.nextftc.ftc.Gamepads.gamepad2
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.ServoEx
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.update
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem

@Configurable
object Kickstand : Subsystem() {
    var LEFT_MIN = 0.25
    var LEFT_MAX = 0.95
    var RIGHT_MIN = 0.30
    var RIGHT_MAX = 1.00
    var DISENGAGE = 0.0
    var ENGAGE = 1.0
    var POS = DISENGAGE

    val servoLeft = ServoEx("kickstandLeft") { scaleRange(LEFT_MIN, LEFT_MAX) }
    val servoRight = ServoEx("kickstandRight") { scaleRange(RIGHT_MIN, RIGHT_MAX); direction = REVERSE }

    val engage by instant { POS = ENGAGE }
    val disengage by instant { POS = DISENGAGE }
    val toggle by instant { POS = if (POS == ENGAGE) DISENGAGE else ENGAGE }

    override fun initialize() {
        POS = DISENGAGE
    }

    override fun controls() {
        val driver = !gamepad1.start and gamepad1.leftBumper and gamepad1.rightBumper
        val operator = !gamepad2.start and gamepad2.leftBumper and gamepad2.rightBumper
        driver whenBecomesTrue toggle
        operator whenBecomesTrue toggle
    }

    override fun periodic() {
        servoLeft.update { position = POS }
        servoRight.update { position = POS }
    }
}
