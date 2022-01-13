package steps

import components.CellType
import components.MatrixMap
import kotlin.random.Random

class ObstacleMapManager(private val matrixMap: MatrixMap) {
    init {
        randomizeObstacles()
        for (i in 0 until Constants.CELLULAR_AUTOMATA_STEPS)
            cellularAutomataStep()
    }


    /**
     * place random obstacles throughout the map
     */
    private fun randomizeObstacles() {
        for (list in matrixMap.matrix)
            for (cell in list)
                if (!Constants.SUPER_EMPTY.contains(cell.cellType)
                    && !Constants.SUPER_OBSTACLES.contains(cell.cellType)
                    && Random.nextFloat() > Constants.RANDOM_OBSTACLE_CHANCE
                )
                    cell.cellType = CellType.OBSTACLE
    }

    /**
     * assign obstacle to cell if its neighbors are obstacles
     */
    fun cellularAutomataStep() {
        for (list in matrixMap.matrix)
            for (cell in list) {
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
                    else if (Random.nextFloat() > Constants.RATIO_OBSTACLE_CHANCE)
                        cell.cellType = CellType.OBSTACLE
                }
            }
    }
}