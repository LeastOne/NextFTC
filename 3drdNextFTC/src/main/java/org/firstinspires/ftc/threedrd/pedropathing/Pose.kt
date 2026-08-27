package org.firstinspires.ftc.threedrd.pedropathing

import com.pedropathing.geometry.Pose
import dev.nextftc.core.units.Distance
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun Pose.axial(distance: Double) = Pose(
    x + cos(heading) * distance,
    y + sin(heading) * distance,
    heading,
    coordinateSystem
)

fun Pose.axial(distance: Distance) = axial(distance.inIn)

fun Pose.lateral(distance: Double) = Pose(
    x + cos(heading + PI / 2) * distance,
    y + sin(heading + PI / 2) * distance,
    heading,
    coordinateSystem
)

fun Pose.lateral(distance: Distance) = lateral(distance.inIn)

fun Pose.midpoint(other: Pose) = Pose(
    (x + other.x) / 2,
    (y + other.y) / 2,
    heading + (other.heading - heading).normalizeHeading() / 2,
    coordinateSystem
)

fun Pose.turn(degrees: Double) = withHeading(
    (heading + Math.toRadians(degrees)).normalizeHeading()
)

fun Pose.reverse(offset: Double = 0.0) = turn(180 + offset)

fun Pose.face(other: Pose, offset: Double = 0.0) = withHeading(
    (atan2(other.y - y, other.x - x) + Math.toRadians(offset)).normalizeHeading()
)

fun Double.normalizeHeading(): Double {
    var heading = this
    while (heading > PI) heading -= 2 * PI
    while (heading < -PI) heading += 2 * PI
    return heading
}
