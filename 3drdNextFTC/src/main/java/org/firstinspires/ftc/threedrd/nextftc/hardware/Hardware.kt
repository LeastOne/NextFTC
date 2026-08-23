package org.firstinspires.ftc.threedrd.nextftc.hardware

interface Hardware {
    val name: String

    fun initialize()
}

object HardwareTelemetry {
    var servo: ServoEx.() -> Unit = {}
    var continuousServo: CRServoEx.() -> Unit = {}
    var motor: MotorEx.() -> Unit = {}
    var imu: IMUEx.() -> Unit = {}
}

fun ServoEx.update(action: ServoEx.() -> Unit) {
    action()
    HardwareTelemetry.servo(this)
}

fun CRServoEx.update(action: CRServoEx.() -> Unit) {
    action()
    HardwareTelemetry.continuousServo(this)
}

fun MotorEx.update(action: MotorEx.() -> Unit) {
    action()
    HardwareTelemetry.motor(this)
}

fun IMUEx.update(action: IMUEx.() -> Unit) {
    action()
    HardwareTelemetry.imu(this)
}
