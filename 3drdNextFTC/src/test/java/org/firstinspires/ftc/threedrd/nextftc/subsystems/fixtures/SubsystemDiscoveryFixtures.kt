package org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures

import org.firstinspires.ftc.threedrd.nextftc.subsystems.Subsystem

object DiscoveredSubsystem : Subsystem()
abstract class AbstractSubsystem : Subsystem()
class ConstructedSubsystem : Subsystem()
class NotASubsystem

class NestedSubsystem {
    object Instance : Subsystem()
}

class InvalidInstanceSubsystem : Subsystem() {
    companion object {
        @JvmField
        val INSTANCE = "Not a subsystem"
    }
}
