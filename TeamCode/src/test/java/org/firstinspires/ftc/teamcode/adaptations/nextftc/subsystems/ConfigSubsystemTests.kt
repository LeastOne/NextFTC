package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Setting
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Level.OFF as LOG_OFF
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.OFF as TEL_OFF
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem.Change.NEXT
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigSubsystemTests {
    private object MinimalConfig : ConfigSubsystem() {
        class Values(@Setting var enabled: Boolean = false)
        override val config = Values()
    }

    @Test
    fun configurationWithoutDiagnosticsDefaultsBothDisplaysOff() {
        ActiveOpMode.it = object : LinearOpMode() {
            override fun runOpMode() = Unit
        }

        MinimalConfig.initialize()

        assertEquals(TEL_OFF, Telemetry.LEVEL)
        assertEquals(LOG_OFF, Logging.LEVEL)
        assertEquals(listOf("Enabled"), MinimalConfig.items.map { it.key })
    }

    @Test
    fun configurationCanUseTheDefaultChangeHook() {
        MinimalConfig.state.configurable = true
        MinimalConfig.config.enabled = false

        MinimalConfig.changeValue(NEXT)

        assertEquals(true, MinimalConfig.config.enabled)
    }
}
