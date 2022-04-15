package steps.map.`object`

import util.Constants.matrixMap
import components.Cell
import components.Fraction

class Castle(
    val fraction: Fraction,
    position: Pair<Int, Int>,
    playerColor: Int,
    val orientation: String
) :
    Building(position, playerColor) {
    override fun getCells(): List<Cell> {
        val res = mutableListOf<Cell>()
        if (orientation == "small_01")
            for (x in -2..2)
                for (y in -2..0)
                    res.add(matrixMap.matrix[position.first + x, position.second + y])
        else
            for (x in -2..0)
                for (y in -2..2)
                    res.add(matrixMap.matrix[position.first + x, position.second + y])
        return res
    }

    override fun getEntrance(): Cell {
        return matrixMap.matrix[position.first, position.second]
    }
}