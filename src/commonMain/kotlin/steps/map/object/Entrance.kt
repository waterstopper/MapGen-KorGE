package steps.map.`object`

import Constants.matrixMap
import components.Cell

class Entrance(position: Pair<Int, Int>) : Building(position) {

    override fun getCells(): List<Cell> = listOf(matrixMap.matrix[position.first, position.second])
    override fun getEntrance(): Cell = matrixMap.matrix[position.first, position.second]
}