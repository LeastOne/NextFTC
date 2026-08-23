package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.Command
import kotlin.reflect.KProperty
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

abstract class DeferredCommandFactory<T : DeferredCommandFactory<T>>(vararg requirements: Any) {
    val requirements = requirements
    lateinit var name: String

    @Suppress("UNCHECKED_CAST")
    operator fun provideDelegate(owner: Subsystem, property: KProperty<*>) = apply {
        name = "${owner.javaClass.simpleName}.${property.name}"
    } as T

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(owner: Subsystem, property: KProperty<*>) = this as T

    fun create(command: () -> Command) = DeferredCommand(*requirements, create = command).named(name)
}

class DeferredCommandFactory1<A>(
    vararg requirements: Any,
    val command: (A) -> Command
) : DeferredCommandFactory<DeferredCommandFactory1<A>>(*requirements) {
    operator fun invoke(first: A) = create { command(first) }
}

open class DeferredCommandFactory3<A, B, C>(
    vararg requirements: Any,
    val command: (A, B, C) -> Command
) : DeferredCommandFactory<DeferredCommandFactory3<A, B, C>>(*requirements) {
    operator fun invoke(first: A, second: B, third: C) = create { command(first, second, third) }
}

class DefaultedDeferredCommandFactory3<A, B, C>(
    vararg requirements: Any,
    val second: B,
    val third: C,
    val command: (A, B, C) -> Command
) : DeferredCommandFactory<DefaultedDeferredCommandFactory3<A, B, C>>(*requirements) {
    operator fun invoke(first: A) = invoke(first, second, third)
    operator fun invoke(first: A, second: B) = invoke(first, second, third)
    operator fun invoke(first: A, second: B, third: C) = create { command(first, second, third) }
}
