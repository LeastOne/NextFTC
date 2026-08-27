package org.firstinspires.ftc.threedrd.nextftc.subsystems

import java.lang.Runnable
import dev.nextftc.core.commands.Command
import org.firstinspires.ftc.threedrd.nextftc.commands.DeferredCommand
import org.firstinspires.ftc.threedrd.nextftc.commands.DeferredCommandFactory1
import org.firstinspires.ftc.threedrd.nextftc.commands.DeferredCommandFactory3
import org.firstinspires.ftc.threedrd.nextftc.commands.DefaultedDeferredCommandFactory3
import org.firstinspires.ftc.threedrd.nextftc.commands.InstantCommand
import org.firstinspires.ftc.threedrd.nextftc.hardware.Hardware
import org.firstinspires.ftc.threedrd.nextftc.logging.Logger
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Tel

abstract class Subsystem : dev.nextftc.core.subsystems.Subsystem {
    open val order = 0
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

    open fun stop() = Unit

    override fun instant(action: Runnable) =
        InstantCommand(this, action)

    override fun instant(name: String, action: Runnable) =
        InstantCommand(this, action).named(name)

    fun deferred(create: () -> Command) = DeferredCommand(this, create = create)
    fun <A> deferred(create: (A) -> Command) = DeferredCommandFactory1(this, command = create)
    fun <A, B, C> deferred(create: (A, B, C) -> Command) =
        DeferredCommandFactory3(this, command = create)
    fun <A, B, C> deferred(second: B, third: C, create: (A, B, C) -> Command) =
        DefaultedDeferredCommandFactory3(this, second = second, third = third, command = create)
}
