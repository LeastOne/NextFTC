package org.firstinspires.ftc.teamcode.subsystems

import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.BLUE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.GREEN
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.ORANGE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.RED
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.WHITE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.GoBildaPrismDriver
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.GoBildaPrismDriver.Artboard.ARTBOARD_0
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.GoBildaPrismDriver.LayerHeight.LAYER_0
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.PrismAnimations
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.device
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem
import org.firstinspires.ftc.teamcode.game.Alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state

object Lights : Subsystem() {
    var color = RED
    var stripLength = 48

    val prism by device(GoBildaPrismDriver::class.java, "prism") { configure(this) }

    fun set(color: Color) = instant { show(color) }.named("Lights.set")

    override fun periodic() {
        when {
            Intake.full -> show(GREEN)
            state.started && state.teleop && Timing.playTimer.seconds() > 110 -> show(RED)
            state.started && state.teleop && Timing.playTimer.seconds() > 100 -> show(ORANGE)
            state.started && state.teleop && Timing.playTimer.seconds() > 80 -> show(WHITE)
            config.alliance == Alliance.RED -> show(RED)
            config.alliance == Alliance.BLUE -> show(BLUE)
        }

        tel.debug("LEDs", prism.numberOfLEDs)
        tel.debug("FPS", prism.currentFPS)
    }

    fun show(color: Color) {
        if (this.color == color) return
        prism.insertAndUpdateAnimation(LAYER_0, PrismAnimations.Solid(color))
        this.color = color
    }

    fun configure(prism: GoBildaPrismDriver) {
        prism.setStripLength(stripLength)
        prism.insertAndUpdateAnimation(LAYER_0, PrismAnimations.Solid(RED))
        prism.saveCurrentAnimationsToArtboard(ARTBOARD_0)
        prism.setDefaultBootArtboard(ARTBOARD_0)
        prism.enableDefaultBootArtboard(true)
    }
}
