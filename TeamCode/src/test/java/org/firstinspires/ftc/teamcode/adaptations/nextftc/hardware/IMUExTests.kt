package org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware

import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.TelemetryLevel.VERBOSE
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry as TeamTelemetry
import org.firstinspires.ftc.teamcode.subsystems.SubsystemTests
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class IMUExTests : SubsystemTests() {
    @Before
    fun configureTelemetry() {
        TeamTelemetry.LEVEL = VERBOSE
        TeamTelemetry.FILTER = ""
    }

    @Test
    fun initializationResolvesAndConfiguresTheImu() {
        var configured = false
        val imu = IMUEx(configure = { configured = true })

        imu.initialize()

        assertNotNull(imu.imu)
        assertNotNull(imu.imuEx)
        assertTrue(configured)
        assertSame(imu, imu.zeroed())
        verify(imu.imu).resetYaw()
    }

    @Test
    fun telemetryReportsOrientation() {
        val imu = IMUEx("testImu")
        imu.initialize()
        val angles = mock(YawPitchRollAngles::class.java)
        `when`(imu.imu.robotYawPitchRollAngles).thenReturn(angles)
        `when`(angles.getYaw(DEGREES)).thenReturn(10.06)
        `when`(angles.getPitch(DEGREES)).thenReturn(20.04)
        `when`(angles.getRoll(DEGREES)).thenReturn(30.06)
        val telemetry = ActiveOpMode.telemetry
        var updated = false
        clearInvocations(telemetry)

        imu.update { updated = true }

        org.junit.Assert.assertTrue(updated)
        verify(telemetry).addData("D | Test Imu | Yaw (deg)", "10.1" as Any)
        verify(telemetry).addData("D | Test Imu | Pitch (deg)", "20.0" as Any)
        verify(telemetry).addData("D | Test Imu | Roll (deg)", "30.1" as Any)
    }
}
