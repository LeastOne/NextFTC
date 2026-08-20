package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.extensions.pedro.PedroComponent
import dev.nextftc.ftc.NextFTCOpMode
import dev.nextftc.ftc.components.BulkReadComponent
import kotlin.math.max
import org.firstinspires.ftc.teamcode.adaptations.nextftc.bindings.BindingsComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.SubsystemComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.TelemetryComponent
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.Constants
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.PedroDrawingComponent
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Nav

abstract class OpMode : NextFTCOpMode() {
    init {
        addComponents(
            TelemetryComponent,
            BindingsComponent,
            BulkReadComponent,
            PedroComponent(Constants::createFollower),
            PedroDrawingComponent(robotRadius = max(Nav.robotLength.inIn, Nav.robotWidth.inIn) / 2),
            ConfigComponent(config),
            SubsystemComponent.all()
        )
    }
}
