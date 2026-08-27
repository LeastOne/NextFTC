package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.CRServo
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.hardware.impl.CRServoEx as NextCRServoEx

class CRServoEx(
    override val name: String,
    cacheTolerance: Double = 0.01,
    configure: CRServo.() -> Unit = {},
) : NextCRServoEx(
    cacheTolerance,
    {
        hardwareMap
            .get(CRServo::class.java, name)
            .apply(configure)
    },
), Hardware {
    override fun initialize() {
        servo
    }
}
