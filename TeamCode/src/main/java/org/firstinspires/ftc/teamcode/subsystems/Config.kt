package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import dev.nextftc.extensions.pedro.PedroComponent.Companion.follower
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Diagnostics.Level
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Setting
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.SettingItem
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Telemetry
import org.firstinspires.ftc.teamcode.adaptations.pedropathing.resetStartingPose
import org.firstinspires.ftc.teamcode.adaptations.quanomous.Quanomous as QuanomousData
import org.firstinspires.ftc.teamcode.game.Alliance
import org.firstinspires.ftc.teamcode.game.Side

@Configurable
object Config : ConfigSubsystem() {
    override var config = Config()
        private set

    val alliance get() = config.alliance
    val side get() = config.side

    data class Config(
        @Setting
        var alliance: Alliance = Alliance.UNKNOWN,

        @Setting
        var side: Side = Side.UNKNOWN,

        @Setting(options = QuanomousData::class)
        var quanomous: String? = null,

        @Setting(inc = 0.5, min = 0.0, max = 30.0, format = "%.1fs")
        var delay: Double = 0.0,

        @Setting(inc = 0.05, min = 0.0, max = 1.0, format = "%.2f", live = true)
        var responsiveness: Double = 1.0,

        @Setting(live = true)
        var robotCentric: Boolean = true,

        @Setting
        var parkGate: Boolean = false,

        @Setting(inc = 6.0, format = "%.1f in", live = true)
        var goalDistanceOffsetSouth: Double = 0.0,

        @Setting(inc = 6.0, format = "%.1f in", live = true)
        var goalDistanceOffsetNorth: Double = 0.0,

        @Setting(inc = 1.0, format = "%.1f deg", live = true)
        var goalAngleOffsetSouth: Double = 0.0,

        @Setting(inc = 1.0, format = "%.1f deg", live = true)
        var goalAngleOffsetNorth: Double = 0.0,

        @Setting(live = true)
        var level: Level = INFO,

        @Transient
        var filter: String = ""
    )

    override fun initialize() {
        super.initialize()

        if (state.auto) {
            config.alliance = Alliance.UNKNOWN
            config.side = Side.UNKNOWN
            config.quanomous = null
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
        if (config.quanomous.isNullOrBlank()) "Quanomous" else null
    )
}
