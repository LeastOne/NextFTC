package org.firstinspires.ftc.teamcode.adaptations.quanomous

import com.google.gson.JsonParser
import dev.nextftc.core.commands.utility.NullCommand
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class QuanomousProgramTests {
    lateinit var directory: java.io.File

    @Before
    fun setUp() {
        directory = Files.createTempDirectory("quanomous-program").toFile()
        Quanomous.files = QuanomousFiles(directory)
    }

    @After
    fun reset() {
        Quanomous.files = QuanomousFiles()
    }

    @Test
    fun turnsStoredStepsIntoASequentialCommand() {
        val commands = QuanomousProgram(
            mapOf("step" to { step -> NullCommand().named(step["name"].asString) })
        )
        val json = JsonParser().parse(
            "[{\"cmd\":\"step\",\"name\":\"First\"},{\"cmd\":\"step\",\"name\":\"Second\"}]"
        ).asJsonArray
        Quanomous.files.save("auto.json", json)

        val group = commands.load("auto.json") as dev.nextftc.core.commands.groups.SequentialGroup

        assertEquals(listOf("First", "Second"), group.commands.map { it.name })
    }

    @Test
    fun rejectsUnknownCommands() {
        val program = QuanomousProgram(emptyMap())
        val steps = JsonParser().parse("[{\"cmd\":\"missing\"}]").asJsonArray

        assertEquals(
            "Unknown Quanomous command: missing",
            assertThrows(IllegalArgumentException::class.java) { program.create(steps) }.message
        )
    }
}
