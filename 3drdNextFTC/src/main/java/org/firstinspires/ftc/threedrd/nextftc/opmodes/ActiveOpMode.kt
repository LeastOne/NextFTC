package org.firstinspires.ftc.threedrd.nextftc.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.nextftc.ftc.ActiveOpMode

val ActiveOpMode.isAutonomous: Boolean
    get() = it!!.javaClass.isAnnotationPresent(Autonomous::class.java)

val ActiveOpMode.isTeleop: Boolean
    get() = it!!.javaClass.isAnnotationPresent(TeleOp::class.java)
