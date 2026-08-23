package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.utility.InstantCommand as NextInstantCommand
import java.lang.Runnable
import kotlin.reflect.KProperty
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.threedrd.nextftc.logging.LogLevel.DEBUG
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging

class InstantCommand(
    val owner: Subsystem,
    val action: Runnable
) : NextInstantCommand({ if (!owner.disabled) action.run() }) {
    init {
        requires(owner)
    }

    operator fun provideDelegate(owner: Subsystem, property: KProperty<*>) = apply {
        val name = "${owner.javaClass.simpleName}.${property.name}"
        named(name)
        setStart {
            if (owner.disabled) return@setStart
            action.run()
            Logging.add("Commands", DEBUG, "Executed | $name")
        }
    }

    operator fun getValue(owner: Subsystem, property: KProperty<*>) = this
}
