package steps

import components.CellType

object Constants {
    // chance of placing obstacle when randomly placing
    const val RANDOM_OBSTACLE_CHANCE = 0.5
    // chance of placing obstacle on a cellular automata step
    const val RATIO_OBSTACLE_CHANCE = 0.4
    // if neighbor ratio exceeds this value, then a cell is 100% obstacle
    const val OBSTACLE_RATIO = 0.75 // 6/8
    // if neighbor ratio is smaller than this value, a cell is 100% empty
    const val EMPTY_RATIO = 0.5

    const val CELLULAR_AUTOMATA_STEPS = 1

    const val SIDE_COST = 2
    const val DIAG_COST = 3
    const val EMPTY_COST = 0

    val SUPER_OBSTACLES = listOf(CellType.EDGE)
    val SUPER_EMPTY = listOf(CellType.ROAD)
    val OBSTACLES = listOf(CellType.EDGE, CellType.OBSTACLE)
    val EMPTY = listOf(CellType.EMPTY, CellType.ROAD)
}