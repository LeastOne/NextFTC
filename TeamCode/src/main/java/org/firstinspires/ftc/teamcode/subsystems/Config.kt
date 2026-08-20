package org.firstinspires.ftc.teamcode.subsystems

import com.bylazar.configurables.annotations.Configurable
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Diagnostics.Level
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Diagnostics.Level.INFO
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.Setting
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.SettingItem
import org.firstinspires.ftc.teamcode.adaptations.nextftc.subsystems.ConfigSubsystem
import org.firstinspires.ftc.teamcode.adaptations.quanomous.Quanomous
import org.firstinspires.ftc.teamcode.game.Alliance
import org.firstinspires.ftc.teamcode.game.Side

@Configurable
object Config : ConfigSubsystem() {
    override var config = Config()
        private set

    data class Config(
        @Setting
        var alliance: Alliance = Alliance.UNKNOWN,

        @Setting
        var side: Side = Side.UNKNOWN,

        @Setting(inc = 0.5, min = 0.0, max = 30.0, format = "%.1fs")
        var delay: Double = 0.0,

        @Setting(inc = 0.05, min = 0.0, max = 1.0, format = "%.2f", live = true)
        var responsiveness: Double = 1.0,

        @Setting(live = true)
        var robotCentric: Boolean = true,

        @Setting(live = true)
        var level: Level = INFO,

        @Transient
        var filter: String = "",

        @Setting(options = Quanomous::class)
        var quanomous: String? = null
    )

    override fun onChange(item: SettingItem) {
        if (item.key == "Alliance" || item.key == "Side") Auto.locate.start()
    }
}
