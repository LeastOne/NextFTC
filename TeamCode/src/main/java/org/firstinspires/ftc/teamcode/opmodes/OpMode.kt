package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.ftc.NextFTCOpMode
import dev.nextftc.ftc.components.BulkReadComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.SubsystemComponent
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry

abstract class OpMode : NextFTCOpMode() {
    init {
        addComponents(
            Telemetry,
            BulkReadComponent,
            SubsystemComponent.all()
        )
    }
}
