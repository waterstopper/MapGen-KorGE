package steps.map.`object`

import util.Constants
import util.Constants.matrixMap
import util.MatrixExtensions.getCellsInInterval
import util.MatrixExtensions.getMaxMinRadius
import components.*
import external.Template
import kotlin.math.round

/**
 * Class for creating buildings
 */
class BuildingsManager(val zones: List<Zone>) {
    val castles: List<Castle>
        get() = zones.flatMap { it.castles }
    val mines: List<Mine>
        get() = zones.flatMap { it.mines }
    private var teleportIndex = 1
    val buildings: List<Building>
        get() = zones.flatMap { it.castles + it.mines + it.teleports }

    init {
        placeCastles()
        placeMines()
    }

    fun placeTeleports() {
        for (zone in zones) {
            for (connection in zone.connections) {
                if (!connection.resolved) {
                    connection.z1.teleports.add(
                        Teleport(
                            connection.z1.getRandomEmptyCell().position,
                            teleportIndex
                        )
                    )
                    connection.z1.teleports.last().place(connection.z1.teleports.last().position)

                    connection.z2.teleports.add(
                        Teleport(
                            connection.z2.getRandomEmptyCell().position,
                            teleportIndex
                        )
                    )
                    connection.z2.teleports.last().place(connection.z2.teleports.last().position)

                    teleportIndex = teleportIndex % 3 + 1
                    connection.resolved = true
                }
            }
        }
    }

    private fun placeCastles() {
        val centres = calculateCentres()
        for (tZone in Constants.template.zones)
            if (tZone.castles.isNotEmpty()) {
                val castle = tZone.castles.first()
                val (position, orientation) = chooseCastleOrientation(centres[tZone.index])
                zones[tZone.index].castles.add(Castle(castle.fraction, position, castle.player, orientation))
                if (!zones[tZone.index].castles.last().place(position)) {
                    println("can't place castle in zone ${zones[tZone.index]} center")
                    zones[tZone.index].castles.removeLast()
                    continue
                }
                zones[tZone.index].center = centres[tZone.index]
            }
    }

    private fun placeMines() {
        for (tZone in Constants.template.zones) {
            val minMax = getMaxMinRadius(zones[tZone.index].centerCell)
            val baseMineCells = getCellsInInterval(zones[tZone.index].centerCell, 0, minMax.second / 2)
            for (tMine in tZone.mines.filter { Resource.baseResources().contains(it.resource) })
                placeMine(zones[tZone.index], tMine, baseMineCells)
            for (tMine in tZone.mines.filter { !Resource.baseResources().contains(it.resource) })
                placeMine(zones[tZone.index], tMine, baseMineCells)
        }
    }

    private fun placeMine(zone: Zone, tMine: Template.TemplateMine, baseMineCells: MutableList<Cell>) {
        val mine = Mine(
            tMine.resource,
            baseMineCells.random(Constants.rnd).position,
            zone,
            tMine.guardLevel.value
        )
        zone.mines.add(mine)
        var attempts = 50
        while (!mine.place(baseMineCells.random(Constants.rnd).position) && attempts > 0) {
            attempts--
        }
        if (attempts == 0) {
            println("can't place mine ${zone.mines.last()}")
            zone.mines.removeLast()
        }
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