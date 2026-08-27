package org.firstinspires.ftc.threedrd.nextftc.config

class SettingItem(
    val key: String,
    val value: () -> Any?,
    val change: (Int) -> Boolean,
    val live: Boolean
)
