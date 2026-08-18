package org.firstinspires.ftc.teamcode.adaptations.pedropathing

import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import dev.nextftc.hardware.driving.DriverControlledCommand
import java.util.function.Supplier

class PedroDriverControlled(
    drivePower: Supplier<Double>,
    strafePower: Supplier<Double>,
    turnPower: Supplier<Double>,
    val robotCentric: () -> Boolean
) : DriverControlledCommand(drivePower, strafePower, turnPower) {
    override fun start() = follower.startTeleopDrive()

    override fun calculateAndSetPowers(powers: DoubleArray) {
        follower.setTeleOpDrive(powers[0], powers[1], powers[2], robotCentric())
    }

    override fun stop(interrupted: Boolean) {
        if (interrupted) follower.breakFollowing()
    }
}
