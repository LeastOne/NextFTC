package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.PedroDrawingComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.teamcode.subsystems.Config
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OpModeTests {
    @Test
    fun compositionCanBeCreated() {
        val opMode = object : OpMode() {}

        assertNotNull(opMode)
        assertTrue(opMode.components.contains(Telemetry))
        assertTrue(opMode.components.any { it is PedroComponent })
        assertTrue(opMode.components.any { it is PedroDrawingComponent })
        val component = opMode.components.filterIsInstance<ConfigComponent<*>>().single()
        assertSame(config, component.config)
        assertEquals("config.json", component.persistence.fileName)
        assertEquals(Config.Config::class.java, component.persistence.type)
    }
}
