package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTests {
    object Options : SettingOptions {
        override fun options() = listOf("First", "Second")
    }

    class ConstructedOptions : SettingOptions {
        override fun options() = listOf("Constructed")
    }

    enum class Choice {
        UNKNOWN, FIRST, SECOND
    }

    enum class SingleChoice {
        UNKNOWN, ONLY
    }

    class Example {
        @Setting(name = "Choice")
        var choice = Choice.UNKNOWN

        @Setting(live = true)
        var robotCentric = false

        @Setting(inc = 0.5, min = 0.0, max = 1.0, format = "%.1f")
        var speed = 0.5

        @Setting(name = "Gain", inc = 0.25)
        var gain = 0.5

        @Setting(options = Options::class)
        var routine: String? = null

        var hidden = "hidden"
    }

    class SingleEnum {
        @Setting
        var choice = SingleChoice.UNKNOWN
    }

    @Test
    fun discoversAndEditsSupportedSettingsInDeclarationOrder() {
        val example = Example()
        val settings = example.settings()

        assertEquals(listOf("Choice", "Robot Centric", "Speed", "Gain", "Routine"), settings.map { it.key })
        assertEquals(Choice.UNKNOWN, settings[0].value())
        assertTrue(settings[1].live)
        assertEquals("0.5", settings[2].value())
        assertEquals(0.5, settings[3].value())
        assertEquals("None", settings[4].value())

        settings[0].change(1)
        assertEquals(Choice.FIRST, example.choice)
        settings[0].change(1)
        assertEquals(Choice.SECOND, example.choice)
        settings[0].change(1)
        assertEquals(Choice.FIRST, example.choice)
        settings[0].change(-1)
        assertEquals(Choice.SECOND, example.choice)

        settings[1].change(1)
        assertTrue(example.robotCentric)
        settings[1].change(-1)
        assertFalse(example.robotCentric)

        settings[2].change(1)
        settings[2].change(1)
        assertEquals(1.0, example.speed, 0.0)
        repeat(3) { settings[2].change(-1) }
        assertEquals(0.0, example.speed, 0.0)

        assertFalse(settings[2].change(-1))

        assertTrue(settings[3].change(1))
        assertEquals(0.75, example.gain, 0.0)
        assertTrue(settings[4].change(1))
        assertEquals("First", example.routine)
        assertEquals("First", settings[4].value())
        assertTrue(settings[4].change(-1))
        assertEquals("Second", example.routine)
        assertEquals("", "".humanize())
        assertEquals("Already", "Already".humanize())
    }

    @Test
    fun reportsWhetherAnEnumActuallyChanged() {
        val setting = SingleEnum().settings().single()

        assertTrue(setting.change(1))
        assertFalse(setting.change(1))
    }

    data class ConstructorSettings(
        @Setting var zebra: Boolean = false,
        @Setting var alpha: Boolean = false,
        @Setting var middle: Boolean = false
    )

    @Test
    fun constructorOrderDoesNotDependOnReflectedFieldOrder() {
        val config = ConstructorSettings()

        val settings = config.settings(
            ConstructorSettings::class.java.declaredFields.reversed()
        )

        assertEquals(listOf("Zebra", "Alpha", "Middle"), settings.map { it.key })
        assertTrue(java.util.Date().settings(emptyList()).isEmpty())
    }

    class Unsupported {
        @Setting
        var number = 1
    }

    class EmptyStringOptions {
        @Setting
        var text: String? = null
    }

    class ConstructedStringOptions {
        @Setting(options = ConstructedOptions::class)
        var text: String? = null
    }

    class MissingIncrement {
        @Setting
        var number = 0.0
    }

    class InvalidIncrement {
        @Setting(inc = -1.0)
        var number = 0.0
    }

    class InvalidRange {
        @Setting(inc = 1.0, min = 2.0, max = 1.0)
        var number = 0.0
    }

    enum class EmptyChoice {
        UNKNOWN
    }

    class EmptyEnum {
        @Setting
        var choice = EmptyChoice.UNKNOWN
    }

    @Test
    fun rejectsInvalidSettingDeclarations() {
        assertEquals(
            "Unsupported @Setting type for number: int",
            assertThrows(IllegalStateException::class.java) { Unsupported().settings() }.message
        )
        assertEquals(
            "@Setting double number requires a positive increment",
            assertThrows(IllegalArgumentException::class.java) { MissingIncrement().settings() }.message
        )
        assertEquals(
            "@Setting double number requires a positive increment",
            assertThrows(IllegalArgumentException::class.java) { InvalidIncrement().settings() }.message
        )
        assertEquals(
            "@Setting double number has an invalid range",
            assertThrows(IllegalArgumentException::class.java) { InvalidRange().settings() }.message
        )
        assertEquals(
            "@Setting enum choice has no selectable values",
            assertThrows(IllegalArgumentException::class.java) { EmptyEnum().settings() }.message
        )
        assertFalse(EmptyStringOptions().settings().single().change(1))
        assertFalse(EmptyStringOptions().settings().single().change(-1))
        val constructed = ConstructedStringOptions()
        assertTrue(constructed.settings().single().change(1))
        assertEquals("Constructed", constructed.text)
        assertFalse(constructed.settings().single().change(1))

        val reverse = Example()
        assertTrue(reverse.settings().last().change(-1))
        assertEquals("Second", reverse.routine)
    }
}
