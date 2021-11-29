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
    var (zones, connections) = t.parse("map.txt")
    zones = zones as MutableList<Zone>
    connections = connections as MutableList<Connection>
//    val z0 = Zone(Biome.GRASS, 50, mutableListOf(), 0)
//    val z1 = Zone(Biome.DIRT, 50, mutableListOf(), 1)
//    val z2 = Zone(Biome.SNOW, 50, mutableListOf(), 2)
//    val z3 = Zone(Biome.LAVA, 50, mutableListOf(), 3)
//    val z4 = Zone(Biome.SWAMP, 50, mutableListOf(), 3)
//    val c01 = Connection(z0, z1, ConnectionType.REGULAR, 1)
//    val c02 = Connection(z0, z2, ConnectionType.REGULAR, 1)
//    val c03 = Connection(z0, z3, ConnectionType.REGULAR, 1)
//    val c04 = Connection(z0, z4, ConnectionType.REGULAR, 1)
//    val c13 = Connection(z1, z3, ConnectionType.REGULAR, 1)
//    val c14 = Connection(z1, z4, ConnectionType.REGULAR, 1)
//
//    z0.connections.addAll(mutableListOf(c01, c02, c03, c04))
//    z1.connections.addAll(mutableListOf(c01, c13, c14))
//    z2.connections.add(c02)
//    z3.connections.addAll(mutableListOf(c03, c13))
//    z4.connections.addAll(mutableListOf(c04, c14))
//    val zones = mutableListOf(z0, z1, z2, z3, z4)

    //placeZoneCircles(zones, mutableListOf(c01, c02, c03, c13, c14), this)

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

    for (i in zone.getPlaced()) {
        i.getConnection(zone).line = lines.line(zone.getCenter(), i.getCenter())

        // TODO: draw connection here and check if it intersects something.
        // OR check after this if
        if (zone.circle.pos.distanceTo(i.circle.pos) >= 3.5 * max(zone.size, i.size)) {
            println(i.type)
            i.toNearestValidPosition(circles)
            for (j in i.connections) {
                if (j.intersectsAny(lines)) {
                    j.line.removeFromParent()
                    println("boo")
                }
            }
        }

    }
}