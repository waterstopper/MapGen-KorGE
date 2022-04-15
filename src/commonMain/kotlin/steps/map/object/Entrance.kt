package steps.map.`object`

import util.Constants.matrixMap
import components.Cell

/**
 * Entrance for all buildings - position of cell to interact with them
 */
class Entrance(position: Pair<Int, Int>) : Building(position) {

    override fun getCells(): List<Cell> = listOf(matrixMap.matrix[position.first, position.second])
    override fun getEntrance(): Cell = matrixMap.matrix[position.first, position.second]
}