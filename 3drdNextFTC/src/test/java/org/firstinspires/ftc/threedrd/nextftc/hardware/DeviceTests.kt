package org.firstinspires.ftc.threedrd.nextftc.hardware

import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.DigitalChannel.Mode.INPUT
import org.firstinspires.ftc.threedrd.testing.SubsystemTests
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceTests : SubsystemTests() {
    @Test
    fun initializationResolvesAndConfiguresAnArbitraryDevice() {
        var configured = false
        val device = device(DigitalChannel::class.java, "laser") {
            mode = INPUT
            configured = true
        }

        device.initialize()

        assertEquals("laser", device.name)
        assertEquals(DigitalChannel::class.java, device.type)
        assertNotNull(device.device)
        assertTrue(configured)
    }

    @Test
    fun delegatesToTheResolvedDevice() {
        val device = device(DigitalChannel::class.java, "laser")
        device.initialize()
        val owner = object {
            val laser by device
        }

        assertSame(device.device, owner.laser)
    }
}
