package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Setting(
    val name: String = "",
    val inc: Double = Double.NaN,
    val min: Double = Double.NEGATIVE_INFINITY,
    val max: Double = Double.POSITIVE_INFINITY,
    val format: String = "",
    val live: Boolean = false
)
