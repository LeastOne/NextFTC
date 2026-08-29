package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.*
import org.firstinspires.ftc.teamcode.subsystems.Auto.execute

@Autonomous
@Suppress("unused")
class Auto : OpMode() {
    override fun onStartButtonPressed() {
        execute().schedule()
    }
}
