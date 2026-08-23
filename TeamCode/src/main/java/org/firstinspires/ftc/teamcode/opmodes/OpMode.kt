package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.NextFTCOpMode
import dev.nextftc.ftc.components.BulkReadComponent
import org.firstinspires.ftc.threedrd.nextftc.bindings.BindingsComponent
import org.firstinspires.ftc.threedrd.nextftc.config.ConfigComponent
import org.firstinspires.ftc.threedrd.nextftc.subsystems.SubsystemComponent
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryComponent
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants
import org.firstinspires.ftc.teamcode.adaptations.nextftc.hardware.configureHardwareTelemetry
import org.firstinspires.ftc.threedrd.pedropathing.PedroDrawingComponent
import org.firstinspires.ftc.teamcode.subsystems.Config.config

abstract class OpMode : NextFTCOpMode() {
    init {
        configureHardwareTelemetry()
        addComponents(
            TelemetryComponent,
            BindingsComponent,
            BulkReadComponent,
            PedroComponent(Constants::createFollower),
            PedroDrawingComponent(robotRadius = Constants.robotRadius),
            ConfigComponent(config),
            SubsystemComponent.all()
        )
    }
}
