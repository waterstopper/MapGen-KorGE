package steps.building

import components.*

class Mine(map: MatrixMap, val resource: Resource, position:Pair<Int,Int>) :
    Building(map,position) {
    lateinit var zone: Zone
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

    override fun getCells(): List<Cell> {
        val res = mutableListOf<Cell>()
        when (resource) {
            Resource.ORE -> {
                for (x in -1..0)
                    for (y in -1..0)
                        res.add(map.matrix[position.first + x, position.second + y])
            }
            Resource.WOOD -> {
                for (x in -1..0)
                    for (y in -2..0)
                        res.add(map.matrix[position.first + x, position.second + y])
            }
            Resource.MERCURY -> {
                res.addAll(
                    listOf(
                        map.matrix[position.first, position.second],
                        map.matrix[position.first - 1, position.second],
                        map.matrix[position.first - 1, position.second - 1]
                    )
                )
            }
            else -> {
                for (x in -1..1)
                    for (y in -3..-2)
                        res.add(map.matrix[position.first + x, position.second + y])
                res.addAll(
                    listOf(
                        map.matrix[position.first, position.second],
                        map.matrix[position.first, position.second - 1]
                    )
                )
            }
        }
        return res
    }

    override fun getEntrance(): Cell = map.matrix[position.first, position.second]

}