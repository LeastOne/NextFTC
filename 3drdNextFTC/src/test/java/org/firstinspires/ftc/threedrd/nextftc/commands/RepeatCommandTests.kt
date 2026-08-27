package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.Command
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatCommandTests {
    class CountingCommand : Command() {
        var starts = 0
        var updates = 0
        var stops = mutableListOf<Boolean>()
        override val isDone get() = updates >= starts
        override fun start() { starts++ }
        override fun update() { updates++ }
        override fun stop(interrupted: Boolean) { stops += interrupted }
    }

    @Test
    fun repeatsACommandTheRequestedNumberOfTimes() {
        val child = CountingCommand()
        child.requires("drive")
        val repeat = child.times(3)

        repeat.start()
        assertFalse(repeat.isDone)
        repeat.update()
        repeat.update()
        repeat.update()

        assertTrue(repeat.isDone)
        assertEquals(3, child.starts)
        assertEquals(listOf(false, false, false), child.stops)
        assertEquals(setOf("drive"), repeat.requirements)
        repeat.stop(false)
    }

    @Test
    fun zeroIterationsFinishImmediately() {
        val child = CountingCommand()
        val repeat = child.times(0)

        repeat.start()
        repeat.update()

        assertTrue(repeat.isDone)
        assertEquals(0, child.starts)
    }

    @Test
    fun interruptionStopsTheActiveIteration() {
        val child = object : Command() {
            var interrupted = false
            override val isDone = false
            override fun stop(interrupted: Boolean) { this.interrupted = interrupted }
        }
        val repeat = child.times(2)

        repeat.start()
        repeat.update()
        repeat.stop(true)

        assertTrue(child.interrupted)
        assertFalse(repeat.isDone)
    }

    @Test
    fun rejectsNegativeCounts() {
        assertThrows(IllegalArgumentException::class.java) { CountingCommand().times(-1) }
    }

    @Test
    fun exposesProgressForDiagnostics() {
        val child = CountingCommand()
        val repeat = child.times(2)
        assertEquals(child, repeat.command)
        assertEquals(2, repeat.count)
        repeat.completed = 1
        repeat.running = true
        assertEquals(1, repeat.completed)
        assertTrue(repeat.running)
    }
}
