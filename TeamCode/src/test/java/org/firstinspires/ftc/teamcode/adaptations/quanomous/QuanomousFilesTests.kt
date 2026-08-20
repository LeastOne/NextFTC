package org.firstinspires.ftc.teamcode.adaptations.quanomous

import com.google.gson.JsonParser
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test

class QuanomousFilesTests {
    @Test
    fun savesLoadsAndSortsJsonPrograms() {
        val directory = Files.createTempDirectory("quanomous-files").toFile()
        val files = QuanomousFiles(directory)
        val program = JsonParser().parse("[{\"cmd\":\"score\"}]").asJsonArray

        assertEquals(emptyList<String>(), files.names())
        files.save("b.JSON", program)
        files.save("a.json", program)
        directory.resolve("ignored.txt").writeText("ignored")

        assertEquals(listOf("a.json", "b.JSON"), files.names())
        assertEquals(program, files.load("a.json"))
        assertEquals(directory, files.directory)
    }
}
