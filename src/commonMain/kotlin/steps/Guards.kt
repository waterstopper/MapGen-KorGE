package steps

import util.Constants.rnd
import util.Constants.zones
import components.CellType
import steps.map.`object`.Guard
import steps.map.`object`.MapObject
import util.Constants
import util.Constants.matrixMap

/**
 * For placing treasures and guards
 */
class Guards {
    val guards = mutableListOf<Guard>()
    val treasures = mutableListOf<Treasure>()

    class Treasure(position: Pair<Int, Int>, val type: TreasureType) : MapObject(position)
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
                    matrixMap.matrix[guards.last().position.first, guards.last().position.second].cellType =
                        CellType.GUARD
                }
            }
            z.mines.forEach { mine ->
                if (mine.guardLevel > -1) {
                    val suitableCells = mine.getEntrance().getAllNeighbors()
                        .filter {
                            (it.cellType == CellType.EMPTY || it.cellType == CellType.ROAD)
                                    && !z.buildings.map { building -> building.getEntrance() }.contains(it)
                        }
                    if (suitableCells.isNotEmpty()) {
                        guards.add(Guard(suitableCells.random(rnd).position, mine.guardLevel))
                        matrixMap.matrix[guards.last().position.first, guards.last().position.second].cellType =
                            CellType.GUARD
                    } else println("can't place guard near $mine")
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
                treasures.add(Treasure(cell.position, treasureType))
                richness -= treasureType.cost
            }
        }
    }
}