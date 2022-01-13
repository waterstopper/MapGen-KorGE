import components.CellType

object Constants {
    const val RANDOM_OBSTACLE_CHANCE = 0.5
    const val RATIO_OBSTACLE_CHANCE = 0.4
    const val OBSTACLE_RATIO = 0.75 // 6/8
    const val EMPTY_RATIO = 0.5
    const val CELLULAR_AUTOMATA_STEPS = 1

    val SUPER_OBSTACLES = listOf(CellType.EDGE)
    val SUPER_EMPTY = listOf(CellType.ROAD)
    val OBSTACLES = listOf(CellType.EDGE, CellType.OBSTACLE)
    val EMPTY = listOf(CellType.EMPTY, CellType.ROAD)
}