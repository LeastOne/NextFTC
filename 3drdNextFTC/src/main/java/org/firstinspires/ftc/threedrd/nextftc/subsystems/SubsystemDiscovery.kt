package org.firstinspires.ftc.threedrd.nextftc.subsystems

import dalvik.system.DexFile
import java.lang.reflect.Modifier

object SubsystemDiscovery {
    fun discover(
        classNames: Iterable<String>,
        classLoader: ClassLoader? = Subsystem::class.java.classLoader
    ) = classNames
        .filter { '$' !in it }
        .sorted()
        .mapNotNull { load(it, classLoader) }
        .filter { Subsystem::class.java.isAssignableFrom(it) && !Modifier.isAbstract(it.modifiers) }
        .map {
            (it.fields.firstOrNull { field -> field.name == "INSTANCE" }?.get(null) as? Subsystem)
                ?: error("${it.name} must be a Kotlin object")
        }.toSet()

    fun load(name: String, classLoader: ClassLoader?): Class<*>? = try {
        Class.forName(name, false, classLoader)
    } catch (error: Throwable) {
        if (error.unavailableClass()) null else throw error
    }

    fun Throwable.unavailableClass(): Boolean {
        var error: Throwable? = this
        while (error != null) {
            if (error is ClassNotFoundException || error is LinkageError) return true
            error = error.cause
        }
        return false
    }

    @Suppress("DEPRECATION")
    fun classNames(source: String): List<String> {
        val dex = DexFile(source)
        return try {
            dex.entries().asSequence().toList()
        } finally {
            dex.close()
        }
    }
}
