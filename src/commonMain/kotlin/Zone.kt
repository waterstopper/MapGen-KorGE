import GeometryExtensions.getIntersectMetric
import GeometryExtensions.points
import com.soywiz.korge.input.onUpOutside
import com.soywiz.korge.view.Circle
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.xy
import com.soywiz.korma.geom.Point

/**
 * Sizes are computed proportionally to each other.
 * Zone placement starts with 0 index-zone.
 */
class Zone constructor(var type: Biome, val size: Int, val connections: MutableList<Connection>, val index: Int) :
    GraphPart {
    lateinit var circle: Circle

    /**
     * Will always return existing connection in scope of algorithm
     */
    fun getConnection(zone: Zone): Connection {
        return connections.find { it.z1 === zone || it.z2 === zone }!!
    }

    init {
        if (type == Biome.RANDOM) {
            type = Biome.fromInt((1..5).random())
        }
    }

    fun centerToPoint(source: Point) {
        circle.xy(Point(source.x - size, source.y - size))
    }

    fun getCenter(): Point = Point(circle.x + size, circle.y + size)

    fun getPlaced(): List<Zone> {
        return connections.filter { it.getZone(this)::circle.isInitialized }.map { it.getZone(this) }
    }

    fun getNotPlaced(): List<Zone> {
        return connections.filter { !getPlaced().contains(it.getZone(this)) }.map { it.getZone(this) }
    }

    fun sortByPlaced() {
        connections.sortBy { it.getZone(this)::circle.isInitialized }
    }

    fun move(pos: Point) {
        val prevPos = circle.pos + Point(size, size)
        circle.xy(pos)
        redrawConnections()
//        for (i in connections) {
//            if (i.line.points()[0] == prevPos)
//                i.line.setPoints(pos + Point(size, size), i.line.points()[1])
//            else i.line.setPoints(i.line.points()[0], pos + Point(size, size))
//        }
    }

    fun toNearestValidPosition(circles: Container) {
        val references = getPlaced()
        if (references.size == 1)
            return
        val point = Point(0, 0)
        references.onEach {

            point.x += it.circle.x
            point.y += it.circle.y
        }
        point.x /= references.size
        point.y /= references.size

        circle.xy(point)

        if (references.size == 2) {
            val initial = circle.pos
            circle.xy(
                Point(
                    references[0].circle.y - references[1].circle.y,
                    -references[0].circle.x + references[1].circle.x
                ) + initial
            )
            val metric = circle.getIntersectMetric(circles)
            circle.xy(
                Point(
                    -references[0].circle.y + references[1].circle.y,
                    references[0].circle.x - references[1].circle.x
                ) + initial
            )
            if (metric < circle.getIntersectMetric(circles)) {
                print("metric 1 $metric, metric2 ${circle.getIntersectMetric(circles)}")
                move(
                    Point(
                        references[0].circle.y - references[1].circle.y,
                        -references[0].circle.x + references[1].circle.x
                    ) + initial
                )
            } else move(
                Point(
                    -references[0].circle.y + references[1].circle.y,
                    references[0].circle.x - references[1].circle.x
                ) + initial
            )
        }
    }

    fun redrawConnections() {
        for (i in connections) {
            if (i.isInitialized()) {
                if (i.z1 == this)
                    i.line.setPoints(getCenter(), i.line.points()[1])
                else
                    i.line.setPoints(i.line.points()[0], getCenter())
            }
        }
    }

    /**
     * To see in debug
     */
    override fun toString(): String {
        return "$index, $type, $size"
    }
}

enum class Biome(val color: String) {
    RANDOM("#808080"),
    DIRT("#964B00"),
    GRASS("#378805"),
    LAVA("#FF0000"),
    SNOW("#E6E1E1"),
    SWAMP("#5D6A00");

    companion object {
        fun fromInt(value: Int): Biome {
            return when (value) {
                0 -> RANDOM
                1 -> DIRT
                2 -> GRASS
                3 -> LAVA
                4 -> SNOW
                5 -> SWAMP
                else -> throw IllegalArgumentException("No zoneType for that")
            }
        }
    }
}