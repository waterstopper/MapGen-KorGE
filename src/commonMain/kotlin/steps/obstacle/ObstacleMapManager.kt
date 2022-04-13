package steps.obstacle

import Constants
import Constants.matrixMap
import MatrixExtensions.calculateCosts
import com.soywiz.kds.PriorityQueue
import steps.map.`object`.Building
import components.*

class ObstacleMapManager(val buildings: List<Building>) {

    init {
        randomizeObstacles()
        for (i in 0 until Constants.CELLULAR_AUTOMATA_STEPS)
            cellularAutomataStep()
        cleanBuildings()
    }

    /**
     * place random obstacles throughout the map
     */
    private fun randomizeObstacles() {
        matrixMap.matrix.forEach { cell ->
            if (!Constants.SUPER_EMPTY.contains(cell.cellType)
                && !Constants.SUPER_OBSTACLES.contains(cell.cellType)
                && Constants.rnd.nextFloat() <= Constants.RANDOM_OBSTACLE_CHANCE
            )
                cell.cellType = CellType.OBSTACLE
        }
    }

    private fun cleanBuildings() {
        for (building in buildings) {
            for (cell in building.getCells())
                cell.cellType = CellType.BUILDING
            building.getEntrance().cellType = CellType.EMPTY
        }
    }

    /**
     * assign obstacle to cell if its neighbors are obstacles
     */
    private fun cellularAutomataStep() {
        matrixMap.matrix.forEach { cell ->
            if (!Constants.SUPER_EMPTY.contains(cell.cellType)
                && !Constants.SUPER_OBSTACLES.contains(cell.cellType)
            ) {
                val ratio = cell.getObstacleNeighborsRatio()
                // make obstacle
                if (ratio >= Constants.OBSTACLE_RATIO)
                    cell.cellType = CellType.OBSTACLE
                // make empty
                else if (ratio <= Constants.EMPTY_RATIO)
                    cell.cellType = CellType.EMPTY
                // decide by chance
                else if (Constants.rnd.nextFloat() > Constants.RATIO_OBSTACLE_CHANCE)
                    cell.cellType = CellType.OBSTACLE
                //else cell.cellType = CellType.EMPTY
            }
        }
    }

    /*
    connect regions that have been isolated because of cellular automata steps
    1) find the main area
    2) calculate costs of moving to another open areas
    3) cut through to the cheapest open area
    4) repeat 2-3 until all is reached
    - - - calculating cost - - -
    if
    1) cell is empty
    2) cost to move there is minimal
    3) and cost > 0
    => it is a needed cell
     */
    fun connectRegions() {
        for (zone in matrixMap.zones) {
            val root = findRoot(zone)
            do {
                val res = calculateCosts(root)
            } while (res)
        }
    }

    /**
     * Root should be in zone and all its neighbors should be in zone too
     */
    private fun findRoot(zone: Zone): Cell {
        matrixMap.matrix.data.forEach { cell ->
            if (Constants.EMPTY.contains(cell.cellType)
                && cell.zone == zone
                && cell.getNeighbors().all { neighbor -> neighbor.zone == zone }
            ) return cell
        }

        return matrixMap.matrix[0, 0]
    }

    /**
     * used for finding squares 2x2 and 3x3
     */
    fun findAllNSquares(n: Int): List<Pair<Surface, Cell>> {
        val res = mutableListOf<Pair<Surface, Cell>>()

        for (x in 2 until matrixMap.matrix.width)
            for (y in 2 until matrixMap.matrix.height) {
                var isSquare = true
                val surfType = mutableMapOf<Surface, Int>()
                for (b in Surface.values())
                    surfType[b] = 0
                for (x1 in -(n - 1)..0)
                    for (y1 in -(n - 1)..0) {
                        if (!Constants.OBSTACLES.contains(matrixMap.matrix[x + x1, y + y1].cellType))
                            isSquare = false
                        surfType[matrixMap.matrix[x + x1, y + y1].zone.type] =
                            surfType[matrixMap.matrix[x + x1, y + y1].zone.type]!! + 1
                    }
                if (isSquare)
                    res.add(
                        Pair(
                            surfType.filter { entry -> surfType[entry.key] == surfType.maxOf { m -> m.value } }.keys.random(
                                Constants.rnd
                            ),
                            matrixMap.matrix[x, y]
                        )
                    )
            }
        return res
    }

    fun calculateAllObstacleCells(): Int =
        matrixMap.matrix.data.count { cell -> Constants.OBSTACLES.contains(cell.cellType) }
}
