package steps.map.`object`

import util.Constants.matrixMap
import components.Cell

class Teleport(position: Pair<Int, Int>, val index: Int) : Building(position) {
    override fun getCells(): List<Cell> = listOf(matrixMap.matrix[position.first, position.second])
    override fun getEntrance(): Cell = matrixMap.matrix[position.first, position.second]

    override fun toString(): String {
        return "teleport$index"
    }
}
