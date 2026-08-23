package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.Command
import dev.nextftc.core.commands.groups.ParallelGroup

fun Command.alongWith(vararg commands: Command) = ParallelGroup(this, *commands)
