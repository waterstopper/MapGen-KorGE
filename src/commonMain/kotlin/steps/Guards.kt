package steps

import Constants.rnd
import Constants.zones
import components.CellType
import components.MatrixMap
import steps.map.`object`.Guard
import steps.map.`object`.MapObject

class Guards {
    val guards = mutableListOf<Guard>()
    val treasures = mutableListOf<Treasure>()

    class Treasure(map: MatrixMap, position: Pair<Int, Int>, val type: TreasureType) : MapObject(position)
    enum class TreasureType(var cost: Int, var chance: Double) {
        RESOURCE(1, 0.72),
        CAMPFIRE(2, 0.15),
        CHEST(3, 0.1),
        ARTIFACT(5, 0.03);

        companion object {
            fun getRandomTreasure(): TreasureType {
                var chance = rnd.nextDouble()
                var type = 0
                while (chance > 0) {
                    chance -= values()[type].chance
                    type++
                }
                return values()[--type]
            }

            fun changeChance() {
                val chances = Constants.config.treasureChance
                for ((index, type) in values().withIndex())
                    type.chance = chances[index]
            }

            fun changeCost() {
                val costs = Constants.config.treasureCost
                for ((index, type) in values().withIndex())
                    type.cost = costs[index]
            }
        }
    }

    fun placeGuards() {
        zones.forEach { z -> z.connections.forEach { it.resolved = false } }
        zones.forEach { z ->
            z.connections.forEach {
                if (!it.resolved && it.entrances.size > 0 && it.guardLevel > -1) {
                    it.entrances[0].getEntrance().cellType = CellType.GUARD
                    it.resolved = true
                    guards.add(Guard(it.entrances[0].position, it.guardLevel))
                }
            }
            z.mines.forEach { mine ->
                if (mine.guardLevel > -1) {
                    val suitableCells = mine.getEntrance().getAllNeighbors()
                        .filter { it.cellType == CellType.EMPTY || it.cellType == CellType.ROAD }
                    if (suitableCells.isNotEmpty())
                        guards.add(Guard(suitableCells.random(rnd).position, mine.guardLevel))
                    else println("can't place guard near $mine")
                }
            }
        }
    }

    fun placeTreasures() {
        TreasureType.changeChance()
        TreasureType.changeCost()

        for (z in zones) {
            var richness = z.richness
            while (richness > 0) {
                if (z.cells.isEmpty())
                    break
                val cell = z.getRandomEmptyCell()
                if (cell.position == Pair(-1, -1))
                    break
                val treasureType = TreasureType.getRandomTreasure()
                cell.cellType = CellType.TREASURE
                treasures.add(Treasure(cell.matrix, cell.position, treasureType))
                richness -= treasureType.cost
            }
        }
    }
}