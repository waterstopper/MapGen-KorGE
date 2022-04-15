package steps.map.`object`

import util.Constants.matrixMap
import components.*

class Mine(private val resource: Resource, position: Pair<Int, Int>, val zone: Zone, val guardLevel: Int) :
    Building(position) {
    override fun toString(): String {
        return when (resource) {
            Resource.MERCURY -> "mercury_lab"
            Resource.ORE -> "ore_mine"
            Resource.WOOD -> "wood_sawmill${
                if (zone.type.isSawmillSurface())
                    if (zone.type == Surface.DESERT || zone.type == Surface.SAND || zone.type == Surface.NDESERT) "_beach"
                    else "_${zone.type.name.lowercase()}" else ""
            }"
            else -> "${resource.name.lowercase()}_mine${if (zone.type.isMineSurface()) "_${zone.type.name.lowercase()}" else ""}"
        }
    }

    override fun place(position: Pair<Int, Int>): Boolean {
        when (resource) {
            Resource.ORE, Resource.MERCURY -> if (position.first == 0 || position.second == 0)
                return false
            Resource.WOOD -> if (position.first == 0 || position.second < 2)
                return false
            else -> if (position.first == 0 || position.second < 3 || position.first == matrixMap.matrix.width - 1)
                return false
        }
        val res = super.place(position)
        // make sure that current mine is not obstructing castle entrance
        if (zone.castles.isNotEmpty()
            && zone.castles.first().getEntrance().getNeighbors()
                .none { it.cellType == CellType.EMPTY || it.cellType == CellType.ROAD }
        )
            return false
        return res
    }

    override fun getCells(): List<Cell> {
        val res = mutableListOf<Cell>()
        when (resource) {
            Resource.ORE -> {
                for (x in -1..0)
                    for (y in -1..0)
                        res.add(matrixMap.matrix[position.first + x, position.second + y])
            }
            Resource.WOOD -> {
                for (x in -1..0)
                    for (y in -2..0)
                        res.add(matrixMap.matrix[position.first + x, position.second + y])
            }
            Resource.MERCURY -> {
                res.addAll(
                    listOf(
                        matrixMap.matrix[position.first, position.second],
                        matrixMap.matrix[position.first - 1, position.second],
                        matrixMap.matrix[position.first - 1, position.second - 1]
                    )
                )
            }
            else -> {
                for (x in -1..1)
                    for (y in -3..-2)
                        res.add(matrixMap.matrix[position.first + x, position.second + y])
                res.addAll(
                    listOf(
                        matrixMap.matrix[position.first, position.second],
                        matrixMap.matrix[position.first, position.second - 1]
                    )
                )
            }
        }
        return res
    }

    override fun getEntrance(): Cell = matrixMap.matrix[position.first, position.second]
}