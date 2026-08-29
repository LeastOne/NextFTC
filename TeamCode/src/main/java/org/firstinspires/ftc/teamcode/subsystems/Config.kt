package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.*
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.threedrd.nextftc.config.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.threedrd.pedropathing.resetStartingPose
import org.firstinspires.ftc.threedrd.quanomous.Quanomous as QuanomousData
import org.firstinspires.ftc.teamcode.game.Alliance
import org.firstinspires.ftc.teamcode.game.Side

@Configurable
object Config : ConfigSubsystem() {
    @Setting
    var alliance = Alliance.UNKNOWN

    @Setting
    var side = Side.UNKNOWN

    @Setting(options = QuanomousData::class)
    var quanomous: String? = null

    @Setting(inc = 0.5, min = 0.0, max = 30.0, format = "%.1fs")
    var delay = 0.0

    @Setting(inc = 0.05, min = 0.0, max = 1.0, format = "%.2f", live = true)
    var responsiveness = 1.0

    @Setting(live = true)
    var robotCentric = true

    @Setting
    var parkGate = false

    @Setting(inc = 6.0, format = "%.1f in", live = true)
    var goalDistanceOffsetSouth = 0.0

    @Setting(inc = 6.0, format = "%.1f in", live = true)
    var goalDistanceOffsetNorth = 0.0

    @Setting(inc = 1.0, format = "%.1f deg", live = true)
    var goalAngleOffsetSouth = 0.0

    @Setting(inc = 1.0, format = "%.1f deg", live = true)
    var goalAngleOffsetNorth = 0.0

    @Setting(live = true)
    var level = INFO

    @Transient
    var filter = ""

    override fun initialize() {
        super.initialize()

        if (state.auto) {
            alliance = Alliance.UNKNOWN
            side = Side.UNKNOWN
            quanomous = null
        }
    }

    override fun periodic() {
        val missing = missingAutoSettings()
        if (state.auto && missing.isNotEmpty())
            Telemetry.configWarning("Select ${missing.joinToString(", ")} before starting Auto")
        super.periodic()
    }

    override fun start() {
        val missing = missingAutoSettings()
        check(!state.auto || missing.isEmpty()) {
            "Select ${missing.joinToString(", ")} before starting Auto"
        }

        super.start()
    }

    override fun onChange(item: SettingItem) {
        if (item.key == "Alliance" || item.key == "Side")
            follower.resetStartingPose(Nav.start)
    }

    fun missingAutoSettings() = listOfNotNull(
        if (alliance == Alliance.UNKNOWN) "Alliance" else null,
        if (side == Side.UNKNOWN) "Side" else null,
        if (quanomous.isNullOrBlank()) "Quanomous" else null
    )
}
