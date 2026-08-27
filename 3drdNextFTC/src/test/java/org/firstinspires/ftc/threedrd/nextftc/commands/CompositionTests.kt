package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.utility.NullCommand
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class CompositionTests {
    @Test
    fun alongWithComposesCommandsInParallel() {
        val first = NullCommand().named("First")
        val second = NullCommand().named("Second")

        val group = first.alongWith(second)

        assertArrayEquals(arrayOf(first, second), group.commands)
    }
}
