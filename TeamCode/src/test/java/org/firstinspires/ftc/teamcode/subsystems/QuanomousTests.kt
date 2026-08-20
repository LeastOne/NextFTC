package org.firstinspires.ftc.teamcode.subsystems

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.nextftc.core.commands.groups.SequentialGroup
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Axial
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Lateral
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class QuanomousTests {
    @Test
    fun parsesOptionalArguments() {
        val empty = JsonObject()
        assertFalse(with(Quanomous) { empty.boolean("gate") })
        assertTrue(with(Quanomous) { empty.boolean("gate", true) })
        assertEquals("center", with(Quanomous) { empty.text("axial") })
        assertEquals("other", with(Quanomous) { empty.text("missing", "other") })
        val nullableText = mock(JsonObject::class.java)
        val nullableElement = mock(JsonElement::class.java)
        `when`(nullableText.get("value")).thenReturn(nullableElement)
        `when`(nullableElement.asString).thenReturn(null)
        assertEquals("fallback", with(Quanomous) { nullableText.text("value", "fallback") })
        assertEquals(Axial.CENTER, with(Quanomous) { empty.axial() })
        assertEquals(Lateral.CENTER, with(Quanomous) { empty.lateral() })

        fun options(axial: String, lateral: String) = JsonObject().apply {
            addProperty("gate", true)
            addProperty("axial", axial)
            addProperty("lateral", lateral)
        }
        val frontLeft = options("FRONT", "LEFT")
        assertTrue(with(Quanomous) { frontLeft.boolean("gate") })
        assertEquals(Axial.FRONT, with(Quanomous) { frontLeft.axial() })
        assertEquals(Lateral.LEFT, with(Quanomous) { frontLeft.lateral() })
        val backRight = options("back", "right")
        assertEquals(Axial.BACK, with(Quanomous) { backRight.axial() })
        assertEquals(Lateral.RIGHT, with(Quanomous) { backRight.lateral() })
    }

    @Test
    fun compilesSeasonCommandVariants() {
        val variants = JsonParser().parse("""[
            {"cmd":"deposit","locale":"far","txo":0,"tyo":0},
            {"cmd":"chase","cycles":0}
        ]""").asJsonArray

        val commands = (Quanomous.compiler.compile(variants) as SequentialGroup).commands

        assertEquals(2, commands.size)
    }
}
