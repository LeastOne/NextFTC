package org.firstinspires.ftc.threedrd.nextftc.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.ftc.ActiveOpMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveOpModeTests {
    @Autonomous
    class Auto : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @TeleOp
    class Teleop : LinearOpMode() {
        override fun runOpMode() = Unit
    }

    @Test
    fun identifiesAutonomousOpModes() {
        ActiveOpMode.it = Auto()
        assertTrue(ActiveOpMode.isAutonomous)
        assertFalse(ActiveOpMode.isTeleop)

        ActiveOpMode.it = Teleop()
        assertFalse(ActiveOpMode.isAutonomous)
        assertTrue(ActiveOpMode.isTeleop)
    }
}
