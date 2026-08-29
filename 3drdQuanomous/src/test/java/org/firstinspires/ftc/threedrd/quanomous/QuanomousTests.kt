package org.firstinspires.ftc.threedrd.quanomous

import com.google.gson.JsonArray
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.Base64
import java.util.Date
import java.util.zip.GZIPOutputStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuanomousTests {
    lateinit var directory: java.io.File

    @Before
    fun setUp() {
        directory = Files.createTempDirectory("quanomous").toFile()
        Quanomous.storage = QuanomousStorage(directory)
        Quanomous.now = { Date(0) }
        Quanomous.decoder = { Base64.getDecoder().decode(it) }
        Quanomous.lastHash = null
        Quanomous.lastName = null
    }

    @After
    fun reset() {
        Quanomous.storage = QuanomousStorage()
        Quanomous.now = ::Date
        Quanomous.decoder = { android.util.Base64.decode(it, android.util.Base64.NO_WRAP) }
        Quanomous.lastHash = null
        Quanomous.lastName = null
    }

    @Test
    fun decodesStoresAndDeduplicatesPrograms() {
        val json = "[{\"cmd\":\"score\"}]"
        val encoded = ByteArrayOutputStream().use { output ->
            GZIPOutputStream(output).use { it.write(json.toByteArray()) }
            Base64.getEncoder().encodeToString(output.toByteArray())
        }

        assertEquals(json, Quanomous.decode(encoded))
        assertEquals(JsonArray::class.java, Quanomous.parse(json).javaClass)
        assertEquals("12-31-1800--0001.json", Quanomous.filename())

        val first = Quanomous.process(encoded)
        val duplicate = Quanomous.process(encoded)

        assertEquals("12-31-1800--0001.json", first)
        assertEquals(first, duplicate)
        assertEquals(listOf(first), Quanomous.options())
        assertEquals(Quanomous.parse(json), Quanomous.load(first))
        assertEquals(8, Quanomous.hash(Quanomous.parse(json)).length)
    }
}
