package steps.building

import Constants
import MatrixExtensions.getCellsAtRadius
import MatrixExtensions.getCellsInInterval
import MatrixExtensions.getMaxMinRadius
import components.*
import external.Template
import kotlin.math.round

class BuildingsManager(val matrixMap: MatrixMap, val zones: List<Zone>) {
    val castles: List<Castle>
        get() = zones.flatMap { it.castles }
    val mines: List<Mine>
        get() = zones.flatMap { it.mines }
    val teleports: MutableList<Teleport> = mutableListOf()
    var teleportIndex = 1
    val buildings: List<Building>
        get() = zones.flatMap { it.castles + it.mines } + teleports


    init {
        placeCastles()
        placeMines()
    }

    fun placeTeleports() {
        for (zone in zones) {
            for (connection in zone.connections) {
                if (!connection.resolved) {
                    teleports.add(Teleport(matrixMap, connection.z1.getRandomEmptyCell().position, teleportIndex))
                    teleports.add(Teleport(matrixMap, connection.z2.getRandomEmptyCell().position, teleportIndex))
                    teleportIndex = teleportIndex % 3 + 1
                    connection.resolved = true
                }
            }
        }
    }

    private fun placeCastles() {
        val centres = calculateCentres()
        for (tZone in Constants.template.zones)
            for (castle in tZone.castles) {
                val (position, orientation) = chooseCastleOrientation(centres[tZone.index])
                zones[tZone.index].castles.add(
                    Castle(
                        matrixMap,
                        castle.fraction,
                        position,
                        castle.player,
                        orientation
                    )
                )
                zones[tZone.index].castles.last().cellsToBuilding()
                zones[tZone.index].center = centres[tZone.index]
            }
    }

    private fun placeMines() {
        for (tZone in Constants.template.zones) {
            val minMax = getMaxMinRadius(zones[tZone.index].centerCell)
            val baseMineCells = getCellsInInterval(zones[tZone.index].centerCell, 0, minMax.second / 2)
            for (tMine in tZone.mines.filter { Resource.baseResources().contains(it.resource) }) {
                placeMine(zones[tZone.index], tMine, baseMineCells)
            }

            for (tMine in tZone.mines.filter { !Resource.baseResources().contains(it.resource) }) {
                placeMine(zones[tZone.index], tMine, baseMineCells)
            }
//            for (mine in tZone.mines) {
//                val baseMineCells = getCellsAtRadius(
//                    6,
//                    zones[tZone.index].matrixMap.matrix[zones[tZone.index].center.first, zones[tZone.index].center.second]
//                )
//                val woodPos = Constants.rnd.nextInt(0, baseMineCells.lastIndex / 2)
//                zones[tZone.index].mines.add(
//                    Mine(
//                        zones[tZone.index].matrixMap, Resource.WOOD,
//                        baseMineCells[woodPos].position
//                    )
//                )
//                zones[tZone.index].mines.add(
//                    Mine(
//                        zones[tZone.index].matrixMap, Resource.ORE,
//                        baseMineCells[woodPos + baseMineCells.lastIndex / 2].position
//                    )
//                )
////                val specialMineCells =
////                    getCellsAtRadius(12, castle.map.matrix[castle.position.first, castle.position.second])
////                for (mine in castle.player!!.zone.mines) {
////                    if (mine.resource != Resource.WOOD && mine.resource != Resource.ORE)
////                        mines.add(mine)
////                }
//            }
        }
    }

    private fun placeMine(zone: Zone, tMine: Template.TemplateMine, baseMineCells: MutableList<Cell>) {
        val mine = Mine(
            zone.matrixMap,
            tMine.resource,
            baseMineCells.random(Constants.rnd).position
        )
        zone.mines.add(mine)
        while (baseMineCells.isNotEmpty() && mine.getCells()
                .any { it.cellType == CellType.BUILDING || it.cellType == CellType.EDGE }
        ) {
            baseMineCells.remove(mine.getEntrance())
            mine.position = baseMineCells.random(Constants.rnd).position
        }
        mine.cellsToBuilding()
    }

    private fun calculateCentres(): List<Pair<Int, Int>> {
        val cellCentres = zones.map { mutableListOf(0, 0, 0) }
        matrixMap.matrix.forEach {
            val zone = cellCentres[it.zone.index]
            zone[0] += it.position.first
            zone[1] += it.position.second
            zone[2] += 1
        }
        return cellCentres.map {
            Pair(
                round(it[0].toDouble() / it[2]).toInt(),
                round(it[1].toDouble() / it[2]).toInt()
            )
        }
    }

    private fun chooseCastleOrientation(position: Pair<Int, Int>): Pair<Pair<Int, Int>, String> {
        var res = position
        if (position.first < 2)
            res = Pair(2, res.second)
        else if (matrixMap.matrix.width - position.first < 3)
            res = Pair(matrixMap.matrix.width - 3, res.second)

        if (position.second <= 2)
            res = Pair(position.first, 2)
        else if (matrixMap.matrix.height - position.second < 3)
            res = Pair(res.first, matrixMap.matrix.height - 3)

        return Pair(res, if (Constants.rnd.nextBoolean()) "small_01" else "small_02")
    }
}