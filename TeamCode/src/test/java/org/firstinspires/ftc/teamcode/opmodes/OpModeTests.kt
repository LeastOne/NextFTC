package org.firstinspires.ftc.teamcode.opmodes

import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry
import dev.nextftc.extensions.pedro.PedroComponent
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.PedroDrawingComponent
import org.junit.Assert.assertNotNull
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
    }
}
