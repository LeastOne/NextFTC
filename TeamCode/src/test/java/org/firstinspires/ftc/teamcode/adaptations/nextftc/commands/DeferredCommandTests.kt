package org.firstinspires.ftc.teamcode.adaptations.nextftc.commands

import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.LambdaCommand
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeferredCommandTests {
    private object TestSubsystem : Subsystem() {
        var created = 0
        var arguments = emptyList<Int>()
        val example by deferred {
            created++
            LambdaCommand()
        }
        val one by deferred { first: Int ->
            arguments = listOf(first)
            LambdaCommand()
        }
        val three by deferred { first: Int, second: Int, third: Int ->
            arguments = listOf(first, second, third)
            LambdaCommand()
        }
        val defaulted by deferred(2, 3) { first: Int, second: Int, third: Int ->
            arguments = listOf(first, second, third)
            LambdaCommand()
        }
    }

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
    fun delegatedCommandsInferTheirSubsystemQualifiedName() {
        TestSubsystem.created = 0

        assertEquals("TestSubsystem.example", TestSubsystem.example.name)
        assertTrue(TestSubsystem.example.requirements.contains(TestSubsystem))
        assertEquals(0, TestSubsystem.created)

        TestSubsystem.example.start()
        assertEquals(1, TestSubsystem.created)
        TestSubsystem.example.stop(false)
    }

    @Test
    fun delegatedFactoriesCreateFreshNamedCommandsWithArguments() {
        TestSubsystem.arguments = emptyList()
        val first = TestSubsystem.one(1)
        val second = TestSubsystem.one(2)

        assertNotSame(first, second)
        assertEquals("TestSubsystem.one", first.name)
        assertTrue(first.requirements.contains(TestSubsystem))
        assertTrue(TestSubsystem.arguments.isEmpty())

        first.start()
        assertEquals(listOf(1), TestSubsystem.arguments)
        first.stop(false)
        second.start()
        assertEquals(listOf(2), TestSubsystem.arguments)
        second.stop(false)

        TestSubsystem.three(3, 4, 5).run { start(); stop(false) }
        assertEquals(listOf(3, 4, 5), TestSubsystem.arguments)
    }

    @Test
    fun delegatedFactoriesSupportTrailingDefaults() {
        TestSubsystem.defaulted(1).run { start(); stop(false) }
        assertEquals(listOf(1, 2, 3), TestSubsystem.arguments)

        TestSubsystem.defaulted(1, 4).run { start(); stop(false) }
        assertEquals(listOf(1, 4, 3), TestSubsystem.arguments)

        TestSubsystem.defaulted(1, 4, 5).run { start(); stop(false) }
        assertEquals(listOf(1, 4, 5), TestSubsystem.arguments)
        assertEquals("TestSubsystem.defaulted", TestSubsystem.defaulted(1).name)
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
