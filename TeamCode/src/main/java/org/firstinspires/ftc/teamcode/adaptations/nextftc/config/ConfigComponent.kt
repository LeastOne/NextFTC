package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

import dev.nextftc.core.components.Component
import org.firstinspires.ftc.teamcode.adaptations.ftc.Persistence

class ConfigComponent<T : Any>(
    val config: T,
    val persistence: Persistence<T>
) : Component {
    companion object {
        var onChange: () -> Unit = {}

        fun changed() = onChange()
    }

    constructor(config: T) : this(
        config,
        Persistence(
            "${config.javaClass.simpleName.lowercase()}.json",
            config.javaClass
        )
    )

    override fun preInit() {
        onChange = persistence::changed
        persistence.load(config)
    }

    override fun postWaitForStart() {
        persistence.update(config)
    }

    override fun postUpdate() {
        persistence.update(config)
    }
}
