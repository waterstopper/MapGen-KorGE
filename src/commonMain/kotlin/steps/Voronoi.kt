package steps

import com.soywiz.kmem.nextPowerOfTwo
import com.soywiz.korma.geom.Point
import components.Cell
import components.MatrixMap
import components.Zone
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

class Voronoi {
    fun getMatrixMap(zones: List<Zone>, matrixLength: Int): MatrixMap {
        val bounds = findProperBounds(zones, matrixLength)

        val centers = assignCenters(bounds, zones, matrixLength)

        return buildMatrix(bounds, zones, matrixLength, centers)
    }

    /**
     * Bounds that
     * 1) have equal height and width
     * 2) and each zone center should be at least one cell away from the edge
     * 3) left and top bounds have 0 coordinate
     */
    private fun findProperBounds(zones: List<Zone>, matrixLength: Int): List<Double> {
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
        normalizeBounds(bounds, zones, matrixLength)

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
    private fun normalizeBounds(bounds: MutableList<Double>, zones: List<Zone>, matrixLength: Int) {
        // save 2 cells for the edges
        val oneCell = (bounds[1] - bounds[0]) / (matrixLength - 2)

        for (i in 0..bounds.lastIndex)
            if (i % 2 == 0)
                bounds[i] -= oneCell
            else
                bounds[i] += oneCell
    }

    fun assignCenters(bounds: List<Double>, zones: List<Zone>, matrixLength: Int): List<Pair<Int, Int>> {
        return zones.map {
            val x = (it.circle.pos.x - bounds[0]) / (bounds[1] - bounds[0]) * matrixLength
            val y = (it.circle.pos.y - bounds[2]) / (bounds[3] - bounds[2]) * matrixLength

            Pair(x.roundToInt(), y.roundToInt())
        }
    }

    fun buildMatrix(
        bounds: List<Double>,
        zones: List<Zone>,
        matrixLength: Int,
        centers: List<Pair<Int, Int>>
    ): MatrixMap {

        val matrix = List<MutableList<Cell>>(matrixLength) { mutableListOf() }
        for (i in 0 until matrixLength) {
            for (j in 0 until matrixLength) {
                val ind = findNearest(Pair(i, j), centers)
                matrix[i].add(Cell(ind, zones[ind].type))
            }
        }

        return MatrixMap(matrix)
    }

    private fun findNearest(cell: Pair<Int, Int>, centers: List<Pair<Int, Int>>): Int {
        var nearestInd = 0
        var smallest = Double.MAX_VALUE
        for (i in 0..centers.lastIndex) {
            if (cell.distance(centers[i]) < smallest) {
                smallest = cell.distance(centers[i])
                nearestInd = i
            }
        }
        return nearestInd
    }

    private fun Pair<Int, Int>.distance(other: Pair<Int, Int>): Double {
        return hypot((first - other.first).toDouble(), (second - other.second).toDouble())
    }
}