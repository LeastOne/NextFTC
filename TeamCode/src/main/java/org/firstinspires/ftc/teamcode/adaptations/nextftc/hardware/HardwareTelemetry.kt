package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.LogLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging.log

fun ServoEx.telemetry() {
    log(name, VERBOSE, "pos") { "%.2f".format(position) }
    log(name, VERBOSE, "rev") { servo.direction == REVERSE }
}
