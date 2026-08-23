package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import kotlin.reflect.KProperty

class Device<T : HardwareDevice>(
    override val name: String,
    val type: Class<T>,
    configure: T.() -> Unit,
) : Hardware {
    private val configure = configure

    lateinit var device: T

    override fun initialize() {
        device = hardwareMap.get(type, name).apply(configure)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = device
}

fun <T : HardwareDevice> device(
    type: Class<T>,
    name: String,
    configure: T.() -> Unit = {},
) = Device(name, type, configure)
