package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.hardware.controllable.Controllable
import dev.nextftc.hardware.impl.MotorEx as NextMotorEx

class MotorEx private constructor(
    override val name: String,
    val motorEx: NextMotorEx
) : Controllable by motorEx, Hardware {
    constructor(
        name: String,
        cacheTolerance: Double = 0.01,
        configure: DcMotorEx.() -> Unit = {},
    ) : this(
        name,
        NextMotorEx(
            cacheTolerance,
            {
                hardwareMap
                    .get(DcMotorEx::class.java, name)
                    .apply(configure)
            },
            name
        )
    )

    val motor get() = motorEx.motor
    var velocityPercentage: Double
        get() = velocity / motor.motorType.achieveableMaxTicksPerSecond
        set(value) { motor.velocity = motor.motorType.achieveableMaxTicksPerSecond * value }

    override fun initialize() {
        motor
    }

    fun reversed() = apply { motorEx.reversed() }
    fun zeroed() = apply { motorEx.zeroed() }
    fun atPosition(position: Double) = apply { motorEx.atPosition(position) }
    fun floatMode() = apply { motorEx.floatMode() }
    fun brakeMode() = apply { motorEx.brakeMode() }
}
