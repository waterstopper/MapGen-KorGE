package steps.building

import components.Cell
import components.MatrixMap

class Teleport(map: MatrixMap, position: Pair<Int, Int>, val index: Int) : Building(map, position) {
    override fun getCells(): List<Cell> = listOf(map.matrix[position.first, position.second])
    override fun getEntrance(): Cell = map.matrix[position.first, position.second]

    override fun toString(): String {
        return "teleport$index"
    }
}
