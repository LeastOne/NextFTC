package org.firstinspires.ftc.threedrd.pedropathing

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.hardware.driving.DriverControlledCommand
import java.util.function.Supplier

class PedroDriverControlled(
    drivePower: Supplier<Double>,
    strafePower: Supplier<Double>,
    turnPower: Supplier<Double>,
    val robotCentric: () -> Boolean,
    val headingOffset: () -> Double = { 0.0 }
) : DriverControlledCommand(drivePower, strafePower, turnPower) {
    var drive: (Double) -> Double = { it }
    var strafe: (Double) -> Double = { it }
    var turn: (Double) -> Double = { it }

    override fun start() = follower.startTeleopDrive()

    override fun calculateAndSetPowers(powers: DoubleArray) {
        follower.setTeleOpDrive(
            drive(powers[0]),
            strafe(powers[1]),
            turn(powers[2]),
            robotCentric(),
            headingOffset()
        )
    }

    override fun stop(interrupted: Boolean) {
        if (interrupted) follower.breakFollowing()
    }
}
