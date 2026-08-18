package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

interface Hardware {
    val name: String

    fun initialize()
}

fun ServoEx.update(action: ServoEx.() -> Unit) {
    action()
    tel()
}

fun CRServoEx.update(action: CRServoEx.() -> Unit) {
    action()
    tel()
}

fun MotorEx.update(action: MotorEx.() -> Unit) {
    action()
    tel()
}

fun IMUEx.update(action: IMUEx.() -> Unit) {
    action()
    tel()
}
