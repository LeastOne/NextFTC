package org.firstinspires.ftc.threedrd.util

class Debounce(
    var clock: () -> Double = { System.nanoTime() / 1_000_000_000.0 }
) {
    var previous = false
    var changedAt = clock()

    fun triggered(current: Boolean, threshold: Double): Boolean {
        val triggered = current && !previous && clock() - changedAt >= threshold
        previous = current
        if (current) changedAt = clock()
        return triggered
    }

    fun reset() {
        previous = false
        changedAt = clock()
    }
}
