package org.firstinspires.ftc.threedrd.nextftc.subsystems

import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.NullCommand
import dev.nextftc.core.components.Component
import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.core.subsystems.Subsystem as NextSubsystem
import org.firstinspires.ftc.threedrd.nextftc.opmodes.isTeleop

class SubsystemComponent private constructor(
    subsystems: Set<NextSubsystem>,
    val discoverAll: Boolean
) : Component {
    companion object {
        fun discover(): Set<NextSubsystem> {
            val source = hardwareMap.appContext.applicationInfo.sourceDir
            return SubsystemDiscovery.discover(SubsystemDiscovery.classNames(source))
        }

        var discovery: () -> Set<NextSubsystem> = ::discover
        var discovered: Set<NextSubsystem>? = null

        fun all() = SubsystemComponent(emptySet(), true)
    }

    constructor(vararg subsystems: NextSubsystem) : this(subsystems.toSet(), false)

    var subsystems = ordered(subsystems.flatMap { it.subsystems })

    override fun preInit() {
        if (discoverAll) {
            subsystems = ordered(
                (discovered ?: discovery().also { discovered = it })
                    .flatMap { it.subsystems }
            )
        }

        subsystems.forEach {
            if (it is Subsystem) it.initializeHardware()
            if (it !is Subsystem || !it.disabled) it.initialize()
        }
    }

    override fun preWaitForStart() = updateSubsystems(false)

    override fun preStartButtonPressed() {
        val active = subsystems.filterIsInstance<Subsystem>().filterNot { it.disabled }
        active.forEach { it.start() }
        if (ActiveOpMode.isTeleop) active.forEach { it.controls() }
    }

    override fun preUpdate() = updateSubsystems(true)

    override fun preStop() {
        subsystems.reversed()
            .filterIsInstance<Subsystem>()
            .filterNot { it.disabled }
            .forEach {
                try {
                    it.stop()
                } catch (exception: Exception) {
                    it.log.error("Stop | $exception")
                }
            }
    }

    fun updateSubsystems(scheduleDefaults: Boolean) {
        subsystems.forEach {
            if (it is Subsystem && it.disabled) {
                it.reportDisabled()
                return@forEach
            }

            it.periodic()

            if (scheduleDefaults && !CommandManager.hasCommandsUsing(it)) {
                val command = it.defaultCommand
                if (command !is NullCommand) command.schedule()
            }
        }
    }

    fun ordered(subsystems: Iterable<NextSubsystem>) = subsystems
        .sortedBy { (it as? Subsystem)?.order ?: 0 }
        .toCollection(linkedSetOf())
}
