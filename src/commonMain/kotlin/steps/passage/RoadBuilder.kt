package steps.passage

import MatrixExtensions.getAllDistances
import MatrixExtensions.makeEmptyConnections
import com.soywiz.korma.math.min
import components.Cell
import components.CellType
import components.MatrixMap
import components.Zone
import steps.map.`object`.Castle

class RoadBuilder {
    fun connectCastles(castles: List<Castle>) {
        for (i in 0 until castles.lastIndex) {
            for (j in i..castles.lastIndex)
                makeEmptyConnections(castles[i].getEntrance(), castles[j].getEntrance())
        }
    }

    fun connectGraphs(zones: List<Zone>) {
        zones.forEach { connectGraph(it) }
        //connectGraph(zones[0])
    }

    private fun connectGraph(zone: Zone) {
        val vertices = if (Constants.config.connectTeleportWithRoad.value)
            (zone.buildings.map { it.getEntrance() })
        else (zone.castles.map { it.getEntrance() } + zone.mines.map { it.getEntrance() } + zone.entrances.map { it.getEntrance() })
        val graph = vertices.associateWith { getAllDistances(it, vertices - it) }
        if (graph.any { it.value.size == 0 })
            return
        for (i in 0..vertices.lastIndex) {
            val minDistanceCell =
                graph[vertices[i]]!!//graph.minByOrNull { if (it.value.isNotEmpty()) it.value[0].second else 10000 }!!
            if (minDistanceCell.isEmpty())
                continue
            makeEmptyConnections(vertices[i], minDistanceCell[0].first)

            graph[minDistanceCell[0].first]!!.removeAll { it.first == vertices[i] }
            minDistanceCell.removeAt(0)
        }
    }

    fun normalizeRoads(map: MatrixMap) {
        for (x in 0 until map.matrix.width)
            for (y in 0 until map.matrix.height)
                if (map.matrix[x, y].cellType == CellType.ROAD)
                    if (map.matrix[x, y].position.first > 0 && map.matrix[x, y].position.second > 0)
                        if (map.matrix[x - 1, y].cellType == CellType.ROAD
                            && map.matrix[x, y - 1].cellType == CellType.ROAD
                            && map.matrix[x - 1, y - 1].cellType == CellType.ROAD
                        )
                            map.matrix[x, y].cellType = CellType.EMPTY
        val goodCells = mutableListOf<Cell>()
        for (x in 0 until map.matrix.width)
            for (y in 0 until map.matrix.height)
                if (map.matrix[x, y].cellType == CellType.ROAD
                    && !goodCells.contains(map.matrix[x, y])
                    && isBadRoadCell(map.matrix[x, y], map.zones)
                ) {
                    if (x + 1 != map.matrix.width)
                        goodCells.add(map.matrix[x + 1, y])
                    if (y + 1 != map.matrix.height)
                        goodCells.add(map.matrix[x, y + 1])
                    map.matrix[x, y].cellType = CellType.EMPTY
                }
    }

    private fun isBadRoadCell(cell: Cell, zones: List<Zone>): Boolean {
        if (zones.any { z -> z.buildings.any { it.position == cell.position } })
            return false
        return cell.getNeighbors().count { it.cellType == CellType.ROAD } == 1
    }
}