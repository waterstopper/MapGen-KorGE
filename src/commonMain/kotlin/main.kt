import GeometryExtensions.rotateDegrees
import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point
import kotlin.math.max
import kotlin.math.min

const val height = 320
const val width = 320

suspend fun main() = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    val t = TemplateParser()
    var (zones, connections) = t.parse("sizes.txt")
    zones = zones as MutableList<Zone>
    connections = connections as MutableList<Connection>

    var iter = 0
    val circles = Container()
    val lines = Container()
    stage.addChildren(listOf(circles, lines))

    this.onClick {
        placeZoneCircles(zones, connections, circles, lines, iter)
        iter++
    }
}


fun placeFirst(zones: MutableList<Zone>, circles: Container, lines: Container) {
    var angle = 0
    val z = zones.first()
    z.circle = circles.circle(z.size.toDouble(), Colors[z.type.color])
    z.centerToPoint(Point(width / 2, height / 2))

    for (i in z.connections) {
        i.getZone(z).circle = circles.circle(
            i.getZone(z).size.toDouble(),
            Colors[i.getZone(z).type.color]
        )

        angle += 360 / z.connections.size + (-120 / z.connections.size..120 / z.connections.size).random()

        i.line = lines.line(
            Point(width / 2, height / 2),
            Point(
                width / 2,
                height / 2 - z.size - i.getZone(z).size + (
                        -min(z.size / 3, i.getZone(z).size) / 3..
                                min(z.size / 3, i.getZone(z).size) / 3).random()
            )
        ).rotateDegrees(angle)

        i.getZone(z).centerToPoint(Point(i.line.x2, i.line.y2))
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
            resolveZone(zones[i], circles, lines)
            resolved.add(zones[i])
        }
    }
}

fun placeZoneCircles(zones: MutableList<Zone>, connections: List<Connection>, stage: Stage) {
    val circles = Container()
    val lines = Container()
    stage.addChildren(listOf(circles, lines))

    val resolved = mutableListOf<Zone>()
    zones.sortBy { it.index }
    for (i in zones) {
        i.connections.sortBy { it.type }
    }

    placeFirst(zones, circles, lines)

    for (i in 1..zones.lastIndex) {
        resolveZone(zones[i], circles, lines)
        resolved.add(zones[i])
    }
}

fun resolveZone(zone: Zone, circles: Container, lines: Container) {
    // resolve connections with placed
    for (i in zone.getPlaced()) {
        // not moving resolved zones
        if (zone.getConnection(i).isInitialized()) {
            continue
        }
        i.getConnection(zone).line = lines.line(zone.getCenter(), i.getCenter())
        // if intersects, first try to reposition
        if (i.getConnection(zone).intersectsAny(lines)) {
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

    // TODO place new zones
    for (i in zone.getNotPlaced()) {
        i.circle = circles.circle(
            i.size.toDouble(),
            Colors[i.type.color]
        )
    }
}