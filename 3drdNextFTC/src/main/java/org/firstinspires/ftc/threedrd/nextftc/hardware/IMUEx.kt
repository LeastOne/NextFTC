package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.IMU
import dev.nextftc.core.units.Angle
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.hardware.impl.IMUEx as NextIMUEx
import java.util.function.Supplier

class IMUEx private constructor(
    override val name: String,
    val imuEx: NextIMUEx
) : Supplier<Angle> by imuEx, Hardware {
    constructor(
        name: String = "imu",
        configure: IMU.() -> Unit = {},
    ) : this(
        name,
        NextIMUEx {
            hardwareMap
                .get(IMU::class.java, name)
                .apply(configure)
        }
    )

    val imu get() = imuEx.imu

    override fun initialize() {
        imu
    }

    fun zeroed() = apply { imuEx.zeroed() }
}
