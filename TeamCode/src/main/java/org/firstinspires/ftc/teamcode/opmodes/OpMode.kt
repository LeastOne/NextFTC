package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.NextFTCOpMode
import dev.nextftc.ftc.components.BulkReadComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.bindings.BindingsComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.PedroDrawingComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.SubsystemComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.teamcode.subsystems.Config.config

abstract class OpMode : NextFTCOpMode() {
    init {
        addComponents(
            Telemetry,
            BindingsComponent,
            BulkReadComponent,
            PedroComponent(Constants::createFollower),
            PedroDrawingComponent(),
            ConfigComponent(config),
            SubsystemComponent.all()
        )
    }
}
