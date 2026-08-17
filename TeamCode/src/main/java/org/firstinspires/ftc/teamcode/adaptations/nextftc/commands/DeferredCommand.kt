package org.firstinspires.ftc.teamcode.adaptations.nextftc.commands

import dev.nextftc.core.commands.Command

class DeferredCommand(
    vararg requirements: Any,
    private val create: () -> Command
) : Command() {
    private var command: Command? = null

    init {
        requires(*requirements)
    }

    override fun start() {
        check(command == null) { "DeferredCommand is already running" }

        val created = create()
        check(!created.isScheduled) { "DeferredCommand cannot run an already scheduled command" }
        require(requirements.containsAll(created.requirements)) {
            "DeferredCommand must declare all child command requirements"
        }

        command = created

        try {
            created.start()
        } catch (failure: Throwable) {
            command = null
            throw failure
        }
    }

    override fun update() {
        command?.update()
    }

    override val isDone
        get() = command?.isDone ?: false

    override fun stop(interrupted: Boolean) {
        val active = command ?: return
        try {
            active.stop(interrupted)
        } finally {
            command = null
        }
    }
}

fun deferred(vararg requirements: Any, create: () -> Command) =
    DeferredCommand(*requirements, create = create)
