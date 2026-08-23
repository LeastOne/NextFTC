package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import com.qualcomm.robotcore.util.ElapsedTime
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

@Configurable
object Timing : Subsystem() {
    var ENDGAME_SECONDS = 75.0

    var playTimer = ElapsedTime()
    var periodicTimer = ElapsedTime()
    var rumbled = false

    override fun initialize() {
        playTimer.reset()
        periodicTimer.reset()
        rumbled = false
    }

    override fun start() = playTimer.reset()

    override fun periodic() {
        val milliseconds = periodicTimer.milliseconds()

        if (!rumbled && playTimer.seconds() >= ENDGAME_SECONDS) {
            ActiveOpMode.gamepad1.rumble(1.0, 1.0, 1000)
            ActiveOpMode.gamepad2.rumble(1.0, 1.0, 1000)
            rumbled = true
        }

        tel.debug("Runtime (s)", "%.1f".format(playTimer.seconds()))
        tel.debug("Loop (ms)", "%.0f".format(milliseconds))
        tel.debug("Rate (Hz)", "%.1f".format(1000 / milliseconds))

        periodicTimer.reset()
    }
}
