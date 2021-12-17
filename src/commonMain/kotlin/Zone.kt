import GeometryExtensions.getDegrees
import GeometryExtensions.getIntersectMetric
import GeometryExtensions.points
import com.soywiz.kmem.toIntFloor
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Point
import kotlin.math.abs

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

    fun setCenter(p: Point) {
        circle.pos = Point(p.x - size, p.y - size)
    }

    fun getPlaced(): List<Zone> {
        return connections.filter { it.getZone(this)::circle.isInitialized }.map { it.getZone(this) }
    }

    fun getNotPlaced(): List<Zone> {
        return connections.filter { !getPlaced().contains(it.getZone(this)) }.map { it.getZone(this) }
    }

    fun sortByPlaced() {
        connections.sortBy { it.getZone(this)::circle.isInitialized }
    }

    fun stretchRoad(road: Connection, scale: Float) {
        if (road.z1 == this) {
            road.line.x1 = road.line.x2 + (road.line.x1 - road.line.x2) * scale
            road.line.y1 = road.line.y2 + (road.line.y1 - road.line.y2) * scale
            this.setCenter(road.line.points()[0])
        } else {
            road.line.x2 = road.line.x1 + (road.line.x2 - road.line.x1) * scale
            road.line.y2 = road.line.y1 + (road.line.y2 - road.line.y1) * scale
            this.setCenter(road.line.points()[1])
        }
        redrawConnections()
    }

    private fun closeGap(amount: Int, gapStart: Int, gapEnd: Int): List<Int> {
        val step = abs(gapEnd - gapStart) / (amount + 1)
        val res = mutableListOf<Int>()
        if (amount > 0)
            res.add(gapStart + step)
        for (i in 1..amount) {
            res.add(res[i - 1] + step)
        }
        return res
    }

    /**
     * TODO do this method
     * Returns list of optimal angles for zones that are not drawn yet and connected to this one
     */
    fun getRemainingAngles(): List<Int> {
        val res = mutableListOf<Int>()
        val averageAngle = 360 / connections.size
        val angles = (connections.filter { it.isInitialized() }.map { it.line.getDegrees() }.sorted()).toMutableList()
        if (connections.size - angles.size == 1) {
            return closeGap(connections.size - 1, angles.first(), angles.first() + 360)
        }
        // see how many zones can be placed between two roads
        val gapSizes = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until angles.lastIndex) {
            gapSizes.add(Pair(((angles[i + 1] - angles[i]).toFloat() / (averageAngle * 1.8)).toIntFloor(), i))
        }
        gapSizes.add(Pair(((angles.first() + 360 - angles.last()).toFloat() / (averageAngle * 1.8)).toIntFloor(), angles.lastIndex))
        gapSizes.sortBy { -it.first }
        // to prevent problems with "out of range exception"
        angles.add(angles[0])
        var i = 0
        while (res.size + angles.size - 1 < connections.size) {
            if (i == gapSizes.size) {
                angles.removeAt(angles.lastIndex)
                angles.addAll(res)
                angles.sort()
                val gapSizes = mutableListOf<Pair<Int, Int>>()
                for (i in 0 until angles.lastIndex) {
                    gapSizes.add(Pair(angles[i + 1] - angles[i], i))
                }
                gapSizes.sortBy { -it.first }
                var j = 0
                // can check if next index is smaller more than two times.
                while (connections.size > angles.size) {
                    angles.add(angles[gapSizes[j].second] + gapSizes[j].first / 2)
                    res.add(angles.last())
                    j++
                }
                return res
            }
            res.addAll(
                closeGap(
                    if (connections.size - angles.size - res.size + 1 < gapSizes[i].first)
                        connections.size - angles.size - res.size + 1
                    else gapSizes[i].first,
                    angles[gapSizes[i].second],
                    angles[gapSizes[i].second + 1]
                )
            )
            i++
        }
        return res
    }

    fun move(pos: Point) {
        val prevPos = circle.pos + Point(size, size)
        circle.xy(pos)
        redrawConnections()
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