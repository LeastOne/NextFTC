package org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems

import dev.nextftc.core.commands.CommandManager
import dev.nextftc.core.commands.utility.NullCommand
import dev.nextftc.core.components.Component
import dev.nextftc.ftc.ActiveOpMode.hardwareMap
import dev.nextftc.core.subsystems.Subsystem as NextSubsystem

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

    var subsystems = subsystems.flatMap { it.subsystems }.toSet()

    override fun preInit() {
        if (discoverAll) {
            subsystems = (discovered ?: discovery().also { discovered = it })
                .flatMap { it.subsystems }.toSet()
        }

        subsystems.forEach {
            if (it is Subsystem) it.initializeHardware()
            if (it !is Subsystem || !it.disabled) it.initialize()
        }
    }

    override fun preWaitForStart() = updateSubsystems(false)

    override fun preStartButtonPressed() {
        subsystems.filterIsInstance<Subsystem>().filterNot { it.disabled }.forEach { it.start() }
    }

    override fun preUpdate() = updateSubsystems(true)

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
}
