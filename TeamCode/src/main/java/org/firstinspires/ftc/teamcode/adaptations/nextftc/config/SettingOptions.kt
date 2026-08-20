package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

interface SettingOptions {
    fun options(): List<String>
}

object NoSettingOptions : SettingOptions {
    override fun options() = emptyList<String>()
}
