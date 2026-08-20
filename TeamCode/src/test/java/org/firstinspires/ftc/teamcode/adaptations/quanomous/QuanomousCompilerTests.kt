package org.firstinspires.ftc.teamcode.adaptations.quanomous

import com.google.gson.JsonParser
import dev.nextftc.core.commands.utility.NullCommand
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class QuanomousCompilerTests {
    lateinit var directory: java.io.File

    @Before
    fun setUp() {
        directory = Files.createTempDirectory("quanomous-compiler").toFile()
        Quanomous.storage = QuanomousStorage(directory)
    }

    @After
    fun reset() {
        Quanomous.storage = QuanomousStorage()
    }

    @Test
    fun turnsStoredStepsIntoASequentialCommand() {
        val compiler = QuanomousCompiler(
            mapOf("step" to { step -> NullCommand().named(step["name"].asString) })
        )
        val json = JsonParser().parse(
            "[{\"cmd\":\"step\",\"name\":\"First\"},{\"cmd\":\"step\",\"name\":\"Second\"}]"
        ).asJsonArray
        Quanomous.storage.save("auto.json", json)

        val group = compiler.load("auto.json") as dev.nextftc.core.commands.groups.SequentialGroup

        assertEquals(listOf("First", "Second"), group.commands.map { it.name })
    }

    @Test
    fun rejectsUnknownCommands() {
        val compiler = QuanomousCompiler(emptyMap())
        val steps = JsonParser().parse("[{\"cmd\":\"missing\"}]").asJsonArray

        assertEquals(
            "Unknown Quanomous command: missing",
            assertThrows(IllegalArgumentException::class.java) { compiler.compile(steps) }.message
        )
    }
}
