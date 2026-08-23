package org.firstinspires.ftc.threedrd.ftc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PersistenceTests {
    class Settings {
        var value = 0

        @Transient
        var session = "current"
    }

    object SingletonSettings {
        var value = 0

        @Transient
        var session = "current"

        val fixed = 9
    }

    var time = 0.0

    fun persistence(storage: Storage = mock(Storage::class.java)) =
        Persistence("settings.json", Settings::class.java, storage, 1.0) { time }

    @Test
    fun missingSettingsKeepTheCurrentValue() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)
        val settings = Settings()

        val loaded = persistence.load(settings)

        assertSame(settings, loaded)
        assertFalse(persistence.dirty)
        assertEquals(0.0, persistence.changedAt, 0.0)
        verify(storage).read()
    }

    @Test
    fun loadsSavedFieldsWithoutReplacingTransientState() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)
        val settings = Settings().apply { session = "preserved" }
        val stored = Settings().apply {
            value = 7
            session = "discarded"
        }
        `when`(storage.read()).thenReturn(persistence.snapshot(stored))

        val loaded = persistence.load(settings)

        assertSame(settings, loaded)
        assertEquals(7, loaded.value)
        assertEquals("preserved", loaded.session)
    }

    @Test
    fun persistsMutableSingletonFieldsOnly() {
        SingletonSettings.value = 0
        SingletonSettings.session = "preserved"
        val persistence = Persistence(
            "singleton.json", SingletonSettings::class.java, mock(Storage::class.java)
        ) { time }
        val json = persistence.snapshot(SingletonSettings)

        assertTrue(json.contains("value"))
        assertFalse(json.contains("session"))
        assertFalse(json.contains("fixed"))

        `when`(persistence.storage.read()).thenReturn(
            """{"value":7,"session":"discarded","fixed":0}"""
        )
        persistence.load(SingletonSettings)

        assertEquals(7, SingletonSettings.value)
        assertEquals("preserved", SingletonSettings.session)
        assertEquals(9, SingletonSettings.fixed)
    }

    @Test
    fun nullOrMalformedSettingsAreRecoverable() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)
        val settings = Settings()
        `when`(storage.read()).thenReturn("null")
        assertSame(settings, persistence.load(settings))

        `when`(storage.read()).thenReturn("not json")
        assertSame(settings, persistence.load(settings))
    }

    @Test
    fun savesOnlyAfterAChangeRemainsStable() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)
        val settings = persistence.load(Settings())
        settings.value = 1
        persistence.changed()

        persistence.update(settings)
        time = 0.5
        persistence.changed()
        persistence.update(settings)
        verify(storage, never()).write(anyString())

        time = 1.5
        persistence.update(settings)
        persistence.update(settings)

        verify(storage, times(1)).write(persistence.snapshot(settings))
        assertFalse(persistence.dirty)
    }

    @Test
    fun failedSavesAreRetriedAfterTheDelay() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)
        val settings = persistence.load(Settings())
        settings.value = 1
        persistence.changed()
        persistence.update(settings)
        doThrow(IllegalStateException("disk")).`when`(storage).write(anyString())

        time = 1.0
        persistence.update(settings)
        time = 1.5
        persistence.update(settings)
        verify(storage, times(1)).write(anyString())

        doNothing().`when`(storage).write(anyString())
        time = 2.0
        persistence.update(settings)
        verify(storage, times(2)).write(anyString())
    }

    @Test
    fun exposesItsConfigurationForSimpleAdaptation() {
        val storage = mock(Storage::class.java)
        val persistence = persistence(storage)

        assertEquals("settings.json", persistence.fileName)
        assertEquals(Settings::class.java, persistence.type)
        assertSame(storage, persistence.storage)
        assertEquals(1.0, persistence.delay, 0.0)
        assertSame(persistence.clock, persistence.clock)
        assertSame(persistence.gson, persistence.gson)
        persistence.storage = persistence.storage
        persistence.delay = persistence.delay
        persistence.clock = persistence.clock
        persistence.dirty = persistence.dirty
        persistence.changedAt = persistence.changedAt

        val defaults = Persistence("default.json", Settings::class.java)
        defaults.clock()
        assertEquals("default.json", defaults.storage.file.name)
    }
}
