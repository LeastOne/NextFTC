package org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry

import dev.nextftc.core.components.Component

object TelemetryComponent : Component {
    override fun preInit() = Telemetry.initialize()
    override fun preWaitForStart() = Telemetry.beginFrame()
    override fun preUpdate() = Telemetry.beginFrame()
    override fun postWaitForStart() = Telemetry.update()
    override fun postUpdate() = Telemetry.update()
}
