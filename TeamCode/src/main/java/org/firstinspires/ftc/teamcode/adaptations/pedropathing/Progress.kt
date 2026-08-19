package org.firstinspires.ftc.teamcode.adaptations.pedropathing

@JvmInline
value class PathCompletion(val value: Double)

@JvmInline
value class PathT(val value: Double)

val Number.pct get() = PathCompletion(toDouble() / 100)
val Number.pctT get() = PathT(toDouble() / 100)
