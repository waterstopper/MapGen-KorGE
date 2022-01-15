package steps

import components.Connection
import GeometryExtensions.points
import GeometryExtensions.rotateDegrees
import components.Zone
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.circle
import com.soywiz.korge.view.line
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point
import height
import width
import kotlin.math.min

class Circles {
    /**
     * place 0-index zone in the center
     */
    private fun placeFirst(zones: MutableList<Zone>, circles: Container, lines: Container) {
        var angle = 0
        val z = zones.first()
        z.circle = circles.circle(z.size.toDouble(), z.type.color)
        z.centerToPoint(Point(width / 2, height / 2))

        for (i in z.connections) {
            i.getZone(z).circle = circles.circle(
                i.getZone(z).size.toDouble(),
                i.getZone(z).type.color
            )

            angle += 360 / z.connections.size + (-120 / z.connections.size..120 / z.connections.size).random()
            angle %= 360

            // start from (1, 0) point and rotate by angle counterclockwise
            i.initializeLine(
                lines.line(
                    Point(width / 2, height / 2),
                    Point(
                        width / 2 + z.size + i.getZone(z).size - (
                                -min(z.size / 3, i.getZone(z).size) / 3..
                                        min(z.size / 3, i.getZone(z).size) / 3).random(),
                        height / 2
                    )
                )
            ).rotateDegrees(angle)

//        println(angle%360)
//        println(i.line.getDegrees(z.getCenter()))
//        println(i.line.getDegrees(i.getZone(z).getCenter()))
//        println()

            i.getZone(z).centerToPoint(Point(i.line.x2, i.line.y2))
        }

    }

    fun placeZoneCircles(
        zones: MutableList<Zone>,
        connections: List<Connection>,
        circles: Container,
        lines: Container,
    ) {
        val resolved = mutableListOf<Zone>()
        zones.sortBy { it.index }
        for (i in zones) {
            i.connections.sortBy { it.type }
        }

        placeFirst(zones, circles, lines)

        for (i in 1..zones.lastIndex) {
            resolveZone(zones[i], circles, lines, connections)
            resolved.add(zones[i])
        }
    }


    fun placeZoneCircles(
        zones: MutableList<Zone>,
        connections: List<Connection>,
        circles: Container,
        lines: Container,
        iter: Int
    ) {

        val resolved = mutableListOf<Zone>()
        zones.sortBy { it.index }
        for (i in zones) {
            i.connections.sortBy { it.type }
        }

        if (iter == 0)
            placeFirst(zones, circles, lines)

        for (i in 1..zones.lastIndex) {
            if (iter == i) {
                resolveZone(zones[i], circles, lines, connections)
                resolved.add(zones[i])
            }
        }
    }

//    fun placeZoneCircles(zones: MutableList<Zone>, connections: List<Connection>, stage: Stage) {
//        val circles = Container()
//        val lines = Container()
//        stage.addChildren(listOf(circles, lines))
//
//        val resolved = mutableListOf<Zone>()
//        zones.sortBy { it.index }
//        for (i in zones) {
//            i.connections.sortBy { it.type }
//        }
//
//        placeFirst(zones, circles, lines)
//
//        for (i in 1..zones.lastIndex) {
//            resolveZone(zones[i], circles, lines, connections)
//            resolved.add(zones[i])
//        }
//    }

    private fun resolveZone(zone: Zone, circles: Container, lines: Container, connections: List<Connection>) {
        // resolve connections with placed
        for (i in zone.getPlaced()) {
            // not moving resolved zones
            if (zone.getConnection(i).isInitialized()) {
                continue
            }
            i.getConnection(zone).initializeLine(lines.line(zone.getCenter(), i.getCenter()))
            var intersections = i.getConnection(zone).intersectsList(connections)
            // try to move leaf zone if intersects it
            while (intersections.size == 1) {
                if (intersections[0].z1.connections.size == 1) {
                    intersections[0].z1.stretchRoad(intersections[0], 0.5f)
                } else if (intersections[0].z2.connections.size == 1) {
                    intersections[0].z2.stretchRoad(intersections[0], 0.5f)
                } else {
                    //i.getConnection(zone).line.removeFromParent()
                    break
                }
                intersections = i.getConnection(zone).intersectsList(connections)
            }
            intersections = i.getConnection(zone).intersectsList(connections)
            // if intersects not only leaf, first try to reposition
            if (intersections.isNotEmpty()) {
                val pos = i.circle.pos
                i.toNearestValidPosition(circles)
                // if still intersects, move back and make a portal instead of a road
                if (i.getConnection(zone).intersectsAny(lines)) {
                    i.getConnection(zone).line.removeFromParent()
                    i.circle.pos = pos
                }
            }
            // TODO: here we move zones if they are far away. Not doing it now, but might do later
            // OR check after this if
            // if (zone.circle.pos.distanceTo(i.circle.pos) >= 3.5 * max(zone.size, i.size)) {
//            i.toNearestValidPosition(circles)
//            for (j in i.connections) {
//                if (j.intersectsAny(lines)) {
//                    j.line.removeFromParent()
//                    println("boo")
//                }
//            }
            // }
        }

        var j = 0
        var angles = mutableListOf<Int>()
        if (!zone.getNotPlaced().isEmpty())
            angles = zone.getRemainingAngles().toMutableList()
        // place new zones
        for (i in zone.getNotPlaced()) {
            // lines are first drawn parallel to (1, 0) vector
            i.getConnection(zone).initializeLine(
                lines.line(
                    zone.getCenter(),
                    Point(
                        zone.getCenter().x + i.size + zone.size - (
                                -min(i.size / 3, zone.size) / 3..
                                        min(i.size / 3, zone.size) / 3).random(),
                        zone.getCenter().y
                    )
                )
            ).rotateDegrees(angles[j])
            i.circle = circles.circle(
                i.size.toDouble(),
                i.type.color
            )
            i.setCenter(i.getConnection(zone).line.points()[1])
            j++
        }
    }
}