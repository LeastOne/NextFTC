package org.firstinspires.ftc.teamcode.adaptations.nextftc.commands

import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.LambdaCommand
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeferredCommandTests {
    @After
    fun resetCommands() {
        CommandManager.cancelAll()
        CommandManager.run()
    }

    @Test
    fun createsAtStartAndForwardsTheChildLifecycle() {
        val requirement = Any()
        var created = 0
        var started = 0
        var updated = 0
        var stopped: Boolean? = null
        var done = false
        val child = LambdaCommand()
            .setStart { started++ }
            .setUpdate { updated++ }
            .setStop { stopped = it }
            .setIsDone { done }
        val deferred = deferred(requirement) {
            created++
            child
        }

        assertEquals(0, created)
        assertFalse(deferred.isDone)
        assertTrue(deferred.requirements.contains(requirement))
        deferred.update()
        deferred.stop(false)

        deferred.start()
        assertEquals(1, created)
        assertEquals(1, started)
        deferred.update()
        assertEquals(1, updated)
        assertFalse(deferred.isDone)

        done = true
        assertTrue(deferred.isDone)
        deferred.stop(true)
        assertTrue(stopped!!)

        deferred.start()
        assertEquals(2, created)
        assertEquals(2, started)
        deferred.stop(false)
        assertFalse(stopped)
    }

    @Test
    fun rejectsStartingTwiceWithoutStopping() {
        val deferred = DeferredCommand { LambdaCommand() }

        deferred.start()

        assertThrows(IllegalStateException::class.java) { deferred.start() }
        deferred.stop(false)
    }

    @Test
    fun rejectsAnAlreadyScheduledChild() {
        val child = LambdaCommand().setIsDone { false }
        child.schedule()
        val deferred = DeferredCommand { child }

        assertThrows(IllegalStateException::class.java) { deferred.start() }
    }

    @Test
    fun rejectsUndeclaredChildRequirements() {
        val child = LambdaCommand().requires(Any())
        val deferred = DeferredCommand { child }

        assertThrows(IllegalArgumentException::class.java) { deferred.start() }
    }

    @Test
    fun clearsTheChildEvenWhenStoppingThrows() {
        var created = 0
        val deferred = DeferredCommand {
            created++
            LambdaCommand().setStop {
                if (created == 1) throw IllegalStateException("stop failed")
            }
        }

        deferred.start()
        assertThrows(IllegalStateException::class.java) { deferred.stop(true) }

        deferred.start()
        assertEquals(2, created)
        deferred.stop(false)
    }

    @Test
    fun clearsTheChildWhenStartingThrows() {
        var created = 0
        val deferred = DeferredCommand {
            created++
            LambdaCommand().setStart {
                if (created == 1) throw IllegalStateException("start failed")
            }
        }

        assertThrows(IllegalStateException::class.java) { deferred.start() }

        deferred.start()
        assertEquals(2, created)
        deferred.stop(false)
    }
}
