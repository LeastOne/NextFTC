package org.firstinspires.ftc.threedrd.nextftc.config

import org.firstinspires.ftc.threedrd.ftc.Persistence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.After
import org.junit.Test

class ConfigComponentTests {
    class TestConfig

    class TestPersistence : Persistence<TestConfig>(
        "test.json", TestConfig::class.java
    ) {
        var loads = 0
        var updates = 0
        var changes = 0
        lateinit var value: TestConfig

        override fun load(value: TestConfig): TestConfig {
            loads++
            this.value = value
            return value
        }

        override fun update(value: TestConfig) {
            updates++
            this.value = value
        }

        override fun changed() {
            changes++
        }
    }

    @After
    fun tearDown() {
        ConfigComponent.onChange = {}
    }

    @Test
    fun ownsConfigurationPersistenceLifecycle() {
        val config = TestConfig()
        val persistence = TestPersistence()
        val component = ConfigComponent(config, persistence)

        ConfigComponent.changed()
        component.preInit()
        ConfigComponent.changed()
        component.postWaitForStart()
        component.postUpdate()

        assertSame(config, component.config)
        assertSame(persistence, component.persistence)
        assertSame(config, persistence.value)
        assertEquals(1, persistence.loads)
        assertEquals(1, persistence.changes)
        assertEquals(2, persistence.updates)
    }

    @Test
    fun derivesTheSettingsFileFromTheConfigurationName() {
        val config = TestConfig()
        val component = ConfigComponent(config)

        assertSame(config, component.config)
        assertEquals("testconfig.json", component.persistence.fileName)
        assertEquals(TestConfig::class.java, component.persistence.type)
    }
}
