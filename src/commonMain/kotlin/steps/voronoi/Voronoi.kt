package steps.voronoi

import com.soywiz.kds.Array2
import components.Cell
import components.Connection
import components.MatrixMap
import components.Zone
import steps.posititioning.CircleZone
import util.Constants
import util.Constants.matrixMap
import util.Constants.zones
import kotlin.math.hypot
import kotlin.math.roundToInt

class Voronoi {
    private var circleZones: List<CircleZone> = listOf()

    fun createMatrixMap(circleZones: List<CircleZone>) {
        this.circleZones = circleZones
        matrixMap = initMatrixMap()
        assignEdges()
        //balanceZones()
    }

    private fun createMatrixZones(bounds: List<Double>) {
        val centers = assignCenters(bounds, circleZones)
        for (i in 0..centers.lastIndex) {
            zones.add(Zone(Constants.template.zones[i], centers[i]))
        }
        for (connection in Constants.template.connections) {
            zones[connection.first].connections.add(
                Connection(
                    connection,
                    zones[connection.first],
                    zones[connection.second]
                )
            )
            zones[connection.second].connections.add(zones[connection.first].connections.last())
        }
    }


//    /**
//     * For making zone sizes to be as expected
//     */
//    private fun balanceZones() {
//        // cellSize / matrix^2 -> zone.size / zones.sum(it.size)
//
//        // sort by zone.size/sum * matrix^2 / cellSize. Ideally equals 1
//        var i = 0
//        var sum = 0
//        var smallest = zones[0]
//        println()
//        while (i < 1000) {
//            zones.sortBy { it.cellSize / it.size }
//            if (zones[0] != smallest) {
//                //println(zones[0])
//                smallest = zones[0]
//            }
//            //zones[0].edge.sortBy { it.adjacentEdges.size }
//            val changedCell =
//                zones[0].edge.filter { it.adjacentEdges.size <= zones[0].edge.minOf { cell -> cell.adjacentEdges.size } + 1 }
//                    .random(util.Constants.rnd)
//            val differentZoneNeighbors = changedCell.checkSideNeighbors { c: Cell -> c.zone != changedCell.zone }
//            // if not bridging
//            if (differentZoneNeighbors.isNotEmpty() && !changedCell.isBridgingCell(differentZoneNeighbors[0].zone)) {
//                i++
//                sum++
//            } else {
//
//                //changedCell.isBridgingCell(differentZoneNeighbors[0].zone)
//            }
//            i++
//        }
//        //println(sum)
//    }

    /**
     * Assign edge fields of cells in the matrix
     */
    private fun assignEdges() {
        matrixMap.matrix.forEach { if (it.isAtEdge()) it.zone.edge.add(it) }
        // matrixMap.matrix.forEach { it.adjacentEdges = it.getEdge() }
    }

    private fun initMatrixMap(): MatrixMap {
        createMatrixZones(findProperBounds())

        val res = buildMatrix(zones)

        // init matrix field in cells
        res.matrix.forEach { it.matrix = res }
        for (zone in zones)
            zone.matrixMap = res

        return res
    }

    /**
     * Bounds that
     * 1) have equal height and width
     * 2) and each zone center should be at least one cell away from the edge
     * 3) left and top bounds have 0 coordinate
     */
    private fun findProperBounds(): List<Double> {
        if (circleZones.size == 1)
            return listOf(0.0, 10.0, 0.0, 10.0)

        val z0 = circleZones[0]
        val bounds = mutableListOf(
            z0.circle.pos.x, z0.circle.pos.x,
            z0.circle.pos.y, z0.circle.pos.y
        )
        for (z in circleZones) {
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
        normalizeBounds(bounds)

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
     * Make zone centers not on edge and bound left and right edge as 0
     * matrixLength - amount of cells in a matrix row
     */
    private fun normalizeBounds(bounds: MutableList<Double>) {
        // save 2 cells for the edges
        val oneCell = (bounds[1] - bounds[0]) / (Constants.config.mapSize - 2)

        for (i in 0..bounds.lastIndex)
            if (i % 2 == 0)
                bounds[i] -= oneCell
            else
                bounds[i] += oneCell
    }

    /**
     * Add xy coordinates to circle zone centers
     */
    private fun assignCenters(bounds: List<Double>, zones: List<CircleZone>): List<Pair<Int, Int>> {
        val res = mutableListOf<Pair<Int, Int>>()
        zones.forEach {
            val x = (it.circle.pos.x - bounds[0]) / (bounds[1] - bounds[0]) * Constants.config.mapSize
            val y = (it.circle.pos.y - bounds[2]) / (bounds[3] - bounds[2]) * Constants.config.mapSize

            res.add(Pair(x.roundToInt(), y.roundToInt()))
        }

        return res
    }

    private fun buildMatrix(
        zones: List<Zone>,
    ): MatrixMap {
        val matrix = Array2(Constants.config.mapSize, Constants.config.mapSize, Cell(Pair(0, 0), zones[0]))
        for (i in 0 until matrix.width)
            for (j in 0 until matrix.height) {
                val zone = findNearestZoneCenter(Pair(i, j), zones)
                matrix[i, j] = Cell(Pair(i, j), zone)
                zone.cellSize += 1
            }

        return MatrixMap(matrix, zones)
    }

    private fun findNearestZoneCenter(cell: Pair<Int, Int>, zones: List<Zone>): Zone {
        var nearest = zones[0]
        var smallest = Double.MAX_VALUE
        for (i in 0..zones.lastIndex)
            if (cell.distance(zones[i].center) < smallest) {
                smallest = cell.distance(zones[i].center)
                nearest = zones[i]
            }

        return nearest
    }

    private fun Pair<Int, Int>.distance(other: Pair<Int, Int>): Double =
        hypot((first - other.first).toDouble(), (second - other.second).toDouble())
}