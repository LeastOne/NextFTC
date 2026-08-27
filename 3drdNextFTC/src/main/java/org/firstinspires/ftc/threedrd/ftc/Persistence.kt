package org.firstinspires.ftc.threedrd.ftc

import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.qualcomm.robotcore.util.RobotLog.ii
import com.qualcomm.robotcore.util.RobotLog.ww
import java.lang.reflect.Modifier.FINAL
import java.lang.reflect.Modifier.TRANSIENT

open class Persistence<T : Any>(
    val fileName: String,
    val type: Class<T>,
    var storage: Storage = Storage(fileName),
    var delay: Double = 1.0,
    var clock: () -> Double = { System.nanoTime() / 1_000_000_000.0 }
) {
    val gson = GsonBuilder()
        .excludeFieldsWithModifiers(FINAL, TRANSIENT)
        .setPrettyPrinting()
        .serializeSpecialFloatingPointValues()
        .create()
    var dirty = false
    var changedAt = 0.0

    open fun load(value: T): T {
        val loaded = try {
            val json = storage.read()
            if (json == null) value else {
                GsonBuilder()
                    .excludeFieldsWithModifiers(FINAL, TRANSIENT)
                    .setPrettyPrinting()
                    .serializeSpecialFloatingPointValues()
                    .registerTypeAdapter(type, InstanceCreator { value })
                    .create()
                    .fromJson(json, type)
                    .also { ii("Persistence", "Loaded | $fileName") } ?: value
            }
        } catch (exception: Exception) {
            ww("Persistence", "Load failed | $fileName | $exception")
            value
        }

        dirty = false
        changedAt = clock()
        return loaded
    }

    open fun changed() {
        dirty = true
        changedAt = clock()
    }

    open fun update(value: T) {
        if (dirty && clock() - changedAt >= delay) {
            try {
                storage.write(snapshot(value))
                dirty = false
                ii("Persistence", "Saved | $fileName")
            } catch (exception: Exception) {
                changedAt = clock()
                ww("Persistence", "Save failed | $fileName | $exception")
            }
        }
    }

    fun snapshot(value: T) = gson.toJson(value)
}
