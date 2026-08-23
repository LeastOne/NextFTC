package org.firstinspires.ftc.threedrd.nextftc.commands

import dev.nextftc.core.commands.Command

class RepeatCommand(
    val command: Command,
    val count: Int
) : Command() {
    var completed = 0
    var running = false

    init {
        require(count >= 0) { "Repeat count cannot be negative" }
        setRequirements(command.requirements)
    }

    override fun start() {
        completed = 0
        running = count > 0
        if (running) command.start()
    }

    override fun update() {
        if (!running) return
        command.update()
        if (!command.isDone) return

        command.stop(false)
        completed++
        running = completed < count
        if (running) command.start()
    }

    override val isDone get() = !running && completed == count

    override fun stop(interrupted: Boolean) {
        if (running) command.stop(interrupted)
        running = false
    }
}

fun Command.times(count: Int) = RepeatCommand(this, count)
