package org.firstinspires.ftc.threedrd.nextftc.config

interface SettingOptions {
    fun options(): List<String>
}

object NoSettingOptions : SettingOptions {
    override fun options() = emptyList<String>()
}
