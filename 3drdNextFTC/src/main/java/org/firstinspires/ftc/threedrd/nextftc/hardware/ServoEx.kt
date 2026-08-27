package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.Servo
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.hardware.impl.ServoEx as NextServoEx

class ServoEx(
    override val name: String,
    cacheTolerance: Double = 0.01,
    configure: Servo.() -> Unit = {},
) : NextServoEx(
    cacheTolerance,
    {
        hardwareMap
            .get(Servo::class.java, name)
            .apply(configure)
    },
), Hardware {
    override fun initialize() {
        servo
    }
}
