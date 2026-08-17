package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import java.lang.Runnable
import org.firstinspires.ftc.teamcode.adaptations.nextftc.commands.InstantCommand
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.Hardware
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logger
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Tel

abstract class Subsystem : dev.nextftc.core.subsystems.Subsystem {
    val errors = mutableListOf<String>()
    val disabled get() = errors.isNotEmpty()
    val tel = Tel(javaClass.simpleName)
    val log = Logger(javaClass.simpleName)

    fun reportDisabled() {
        tel.error("Status", "Disabled (see Logcat)")
    }

    fun initializeHardware() {
        errors.clear()
        javaClass.declaredFields.mapNotNull { field ->
            field.isAccessible = true
            field.get(this) as? Hardware
        }.distinct().forEach {
            try {
                it.initialize()
            } catch (exception: Exception) {
                val message = "${it.name}: $exception"
                errors += message
                log.error("Hardware | $message")
            }
        }
    }

    open fun start() = Unit

    open fun controls() = Unit

    override fun instant(action: Runnable) =
        InstantCommand(this, action)

    override fun instant(name: String, action: Runnable) =
        InstantCommand(this, action).named(name)
}
