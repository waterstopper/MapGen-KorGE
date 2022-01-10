package steps

import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import components.Cell
import components.MatrixMap
import components.Zone
import kotlin.math.hypot
import kotlin.math.roundToInt

class Voronoi(val zones: List<Zone>, val matrixLength: Int) {
    val matrixMap = initMatrixMap()

    init {
        //assignEdges()
        //balanceZones()
    }

    /**
     * Used for making zone sizes to be as expected
     */
    fun balanceZones() {

    }

    fun assignEdges() {
        for (list in matrixMap.matrix) {
            for (cell in list) {
                cell.isEdge = cell.isAtEdge()
            }
        }
    }

    fun visualizeMatrix(): Bitmap32 {
        val res = Bitmap32(matrixLength, matrixLength)

        for (x in 0 until matrixLength)
            for (y in 0 until matrixLength) {
                res[x, y] = Colors[matrixMap.matrix[x][y].zone.type.color]
            }
        for (c in matrixMap.zones) {
            res[c.center.first, c.center.second] = Colors.YELLOW
        }
        return res
    }

    fun initMatrixMap(): MatrixMap {
        val bounds = findProperBounds()

        assignCenters(bounds, zones, matrixLength)

        return buildMatrix(zones, matrixLength)
    }

    /**
     * Bounds that
     * 1) have equal height and width
     * 2) and each zone center should be at least one cell away from the edge
     * 3) left and top bounds have 0 coordinate
     */
    private fun findProperBounds(): List<Double> {
        // x min, x max, y min, y max
        val bounds = mutableListOf(50.0, 50.0, 50.0, 50.0)
        for (z in zones) {
            if (z.circle.pos.x < bounds[0])
                bounds[0] = z.circle.pos.x
            if (z.circle.pos.x > bounds[1])
                bounds[1] = z.circle.pos.x
            if (z.circle.pos.y < bounds[2])
                bounds[2] = z.circle.pos.y
            if (z.circle.pos.y > bounds[3])
                bounds[3] = z.circle.pos.y
        }

        makeBoundsSquare(bounds)

        // make centers not on edge
        normalizeBounds(bounds, matrixLength)

        return bounds
    }

    private fun makeBoundsSquare(bounds: MutableList<Double>) {
        val width = bounds[1] - bounds[0]
        val height = bounds[3] - bounds[2]

        if (width > height) {
            bounds[2] -= (width - height) / 2
            bounds[3] += (width - height) / 2
        } else {
            bounds[0] -= (-width + height) / 2
            bounds[1] += (-width + height) / 2
        }
    }

    /**
     * make zone centers not on edge and bound left and right edge as 0
     * matrixLength - amount of cells in a matrix row
     */
    private fun normalizeBounds(bounds: MutableList<Double>, matrixLength: Int) {
        // save 2 cells for the edges
        val oneCell = (bounds[1] - bounds[0]) / (matrixLength - 2)

        for (i in 0..bounds.lastIndex)
            if (i % 2 == 0)
                bounds[i] -= oneCell
            else
                bounds[i] += oneCell
    }

    private fun assignCenters(bounds: List<Double>, zones: List<Zone>, matrixLength: Int) {
        zones.forEach {
            val x = (it.circle.pos.x - bounds[0]) / (bounds[1] - bounds[0]) * matrixLength
            val y = (it.circle.pos.y - bounds[2]) / (bounds[3] - bounds[2]) * matrixLength

            it.center = Pair(x.roundToInt(), y.roundToInt())
        }
    }

    private fun buildMatrix(
        zones: List<Zone>,
        matrixLength: Int
    ): MatrixMap {

        val matrix = List<MutableList<Cell>>(matrixLength) { mutableListOf() }
        for (i in 0 until matrixLength) {
            for (j in 0 until matrixLength) {
                val zone = findNearestZoneCenter(Pair(i, j), zones)
                matrix[i].add(Cell(Pair(i, j), zone))
            }
        }

        return MatrixMap(matrix, zones)
    }

    private fun findNearestZoneCenter(cell: Pair<Int, Int>, zones: List<Zone>): Zone {
        var nearest = zones[0]
        var smallest = Double.MAX_VALUE
        for (i in 0..zones.lastIndex) {
            if (cell.distance(zones[i].center) < smallest) {
                smallest = cell.distance(zones[i].center)
                nearest = zones[i]
            }
        }
        return nearest
    }

    private fun Pair<Int, Int>.distance(other: Pair<Int, Int>): Double {
        return hypot((first - other.first).toDouble(), (second - other.second).toDouble())
    }
}