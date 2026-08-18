package org.firstinspires.ftc.teamcode.subsystems

import dev.nextftc.core.commands.utility.NullCommand
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.Subsystem

object Auto : Subsystem() {
    fun execute() = NullCommand().named("Auto.execute")
}
