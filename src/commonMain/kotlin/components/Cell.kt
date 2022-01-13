package components

import kotlin.math.max
import kotlin.math.min

class Cell(val position: Pair<Int, Int>, val zone: Zone) {

    lateinit var matrix: MatrixMap
    var isEdge = false
    var adjacentEdges: List<Cell> = listOf()
    var cellType = CellType.EMPTY

    /**
     * checks whether cell is at the edge of a map or there is a different zone in 8 neighboring cells
     */
    fun isAtEdge() {
        if (position.first == 0 || position.first == matrix.matrix.lastIndex
            || position.second == 0 || position.second == matrix.matrix.lastIndex
        ) {
            isEdge = true
            cellType = CellType.EDGE
            return
        }
        for (i in position.first - 1..position.first + 1)
            for (j in position.second - 1..position.second + 1) {
                if (matrix.matrix[i][j].zone != zone) {
                    isEdge = true
                    cellType = CellType.EDGE
                    return
                }
            }

        isEdge = false
    }

    /**
     * get all adjacent cells from other zones
     */
    fun getEdge(): List<Cell> {
        val res = mutableListOf<Cell>()
        for (i in max(0, position.first - 1)..min(matrix.matrix.lastIndex, position.first + 1))
            for (j in max(0, position.second - 1)..min(matrix.matrix.lastIndex, position.second + 1)) {
                if (matrix.matrix[i][j].zone != zone)
                    res.add(matrix.matrix[i][j])

            }

        return res
    }

    /**
     * returns how many neighbors are obstacles divided by amount of all neighbors.
     * Ratio is needed because cells on the map edge will obviously have fewer obstacle neighbors
     */
    fun getObstacleNeighborsRatio(): Float {
        var all = 0
        var sum = 0
        for (i in max(0, position.first - 1)..min(matrix.matrix.lastIndex, position.first + 1))
            for (j in max(0, position.second - 1)..min(matrix.matrix.lastIndex, position.second + 1))
                if (i != 0 && j != 0) {
                    if (Constants.OBSTACLES.contains(matrix.matrix[i][j].cellType))
                        sum += 1
                    all += 1
                }

        return sum.toFloat() / all
    }
}

