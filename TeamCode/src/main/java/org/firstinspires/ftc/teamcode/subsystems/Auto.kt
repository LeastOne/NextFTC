package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.geometry.Pose
import dev.nextftc.core.commands.Command
import dev.nextftc.core.commands.delays.Delay
import dev.nextftc.core.commands.delays.WaitUntil
import dev.nextftc.core.units.Distance
import dev.nextftc.core.units.inches
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds
import org.firstinspires.ftc.threedrd.nextftc.commands.alongWith
import org.firstinspires.ftc.threedrd.nextftc.commands.deferred
import org.firstinspires.ftc.threedrd.nextftc.commands.times
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Axial
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Lateral
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.threedrd.pedropathing.tiles
import org.firstinspires.ftc.teamcode.game.Side
import org.firstinspires.ftc.teamcode.game.Side.NORTH
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.side

object Auto : Subsystem() {
    const val TIMEOUT = 29.5

    fun execute() = Delay(config.delay.seconds)
        .then(Quanomous.load(config.quanomous!!))
        .endAfter(TIMEOUT)
        .then(stopAll())

    fun intakeStart() = goalLock(false).then(
        Intake.forward, Conveyor.forward, Gate.close
    )

    fun intakeStop() = goalLock(true).then(
        Flywheel.forward, Conveyor.reverse, Gate.hold
    ).thenWait(0.8).then(
        Conveyor.stop, Intake.hold
    )

    fun intake(spike: Int) = depositStop().then(
        intakeStart(), Drive.intakePower, Drive.toSpike(spike)
    ).thenWait(0.2)

    fun depositStart() = goalLock(true).then(
        Gate.open, Intake.forward, Flywheel.forward, Conveyor.launch
    )

    fun depositStop() = goalLock(false).then(
        Conveyor.stop, Flywheel.stop, Intake.reset, Intake.stop
    )

    fun deposit(side: Side, axial: Distance = 0.inches, lateral: Distance = 0.inches) = deferred(
        Drive, Intake, Conveyor, Flywheel, Gate, Vision
    ) {
        val trigger =
            if (side == NORTH) (-9).inches
            else (if (follower.pose.x < -2.tiles.inIn) (-24).inches else (-48).inches)
        intakeStop().then(
            Drive.autoPower,
            Drive.toDeposit(side, axial, lateral).alongWith(
                Drive.until(trigger).then(
                    Drive.untilHeading(if (follower.pose.x > 2.tiles.inIn) 22.0 else 15.0),
                    if (side == NORTH) Drive.untilNotBusy() else Delay(0.seconds),
                    if (side == NORTH) Drive.untilHeading(4.0).endAfter(1.0) else Delay(0.seconds),
                    if (side == NORTH) Flywheel.untilReady() else Delay(0.seconds),
                    Gate.open,
                    Intake.forward,
                    Flywheel.forward,
                    Conveyor.launch
                ).thenWait(0.4)
            )
        )
    }

    fun releaseGate() = Drive.toGate.alongWith(Gate.close)

    fun gateIntake() = intakeStart().then(
        Drive.toGate, Drive.high, Drive.toGateIntake.endAfter(1.5)
    ).thenWait(1.5).then(
        Intake.untilFull().endAfter(2.0),
        Drive.low, Drive.toGateIntakeDepart.endAfter(0.4), Drive.autoPower
    )

    fun goalLock(enabled: Boolean) = (if (enabled) Vision.goalLock else Vision.goalUnlock).alongWith(
        if (enabled) Drive.goalLock else Drive.goalUnlock
    )

    fun drive(pose: Pose) = depositStop().then(
        Drive.curve(pose)
    )

    fun chase(cycles: Int) = Intake.reset.then(
        chaseCycle().times(cycles)
    )

    fun chaseCycle() = chaseIntake().times(4)
        .raceWith(chaseComplete())
        .then(chaseDeposit())

    fun chaseIntake() = Drive.toChaseScan.alongWith(
        Vision.chaseLock, Vision.waitForElement().endAfter(2.0)
    ).then(
        Vision.backup, intakeStart(), Drive.chaseLock,
        Drive.chaseControlled.raceWith(
            Intake.untilElement(), Drive.untilStill(0.4), Drive.untilAtElement()
        ), Vision.reset
    )

    fun chaseComplete() = Intake.untilFull().raceWith(
        Intake.untilArtifacts(2),
        Intake.untilArtifacts(1).then(
            Drive.untilDepositNorth(-0.75.tiles)
        )
    )

    fun chaseDeposit() = Drive.chaseUnlock.then(
        deposit(side),
    ).thenWait(0.8).then(
        depositStop()
    )

    fun park(gate: Boolean, axial: Axial, lateral: Lateral) =
        WaitUntil(::canPark).endAfter(0.8).then(
            Drive.autoPower,
            Drive.toParking(gate, axial, lateral),
            stopAll()
        )

    fun canPark() = !Drive.goalLocked

    fun stopAll() = goalLock(false).then(
        Drive.chaseUnlock, Drive.stop, Intake.stop, Conveyor.stop, Gate.close, Flywheel.stop
    )

    fun remaining(command: Command) = deferred(*command.requirements.toTypedArray()) {
        command.endAfter(max(0.0, TIMEOUT - Timing.playTimer.seconds()))
    }
}
