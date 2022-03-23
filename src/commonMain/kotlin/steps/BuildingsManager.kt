package steps

import components.*
import kotlin.math.round

class BuildingsManager(val matrixMap: MatrixMap, val zones: List<Zone>, val players: List<Player>) {
    val castles: MutableList<Castle> = mutableListOf()
    val mines: MutableList<Mine> = mutableListOf(Mine(Resource.GEMS, Surface.SAND, Pair(20, 20), null))

    init {
        placeCastles()
    }


    private fun placeCastles() {
        val centres = calculateCentres()
        for (player in players) {
            val (pos, orientation) = chooseCastleOrientation(centres[player.zone.index])
            castles.add(Castle(player.fraction, player, pos, orientation))
            player.castle = castles.last()
        }
    }

    fun calculateCentres(): List<Pair<Int, Int>> {
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