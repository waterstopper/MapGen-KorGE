package components

import external.Template
import steps.map.`object`.Building
import steps.map.`object`.Castle
import steps.map.`object`.Teleport
import steps.map.`object`.Mine
import util.Constants

/**
 * Sizes are computed proportionally to each other.
 * components.Zone placement starts with 0 index-zone.
 */
class Zone constructor(
    private val tZone: Template.TemplateZone,
    var center: Pair<Int, Int>,
    val connections: MutableList<Connection> = mutableListOf()
) {
    val mines = mutableListOf<Mine>()
    val castles = mutableListOf<Castle>()
    val edge = mutableListOf<Cell>()
    val teleports = mutableListOf<Teleport>()

    val entrances
        get() = connections.flatMap { it.entrances }.filter { it.getEntrance().zone == this }
    val centerCell: Cell
        get() = matrixMap.matrix[center.first, center.second]
    val index
        get() = tZone.index
    val type
        get() = tZone.surface
    val buildings: List<Building>
        get() = mines + castles + teleports + entrances
    val richness: Int
        get() = tZone.richness

    lateinit var matrixMap: MatrixMap
    val cells by lazy {
        matrixMap.matrix.filter { it.zone == this }
    }

    // used to see
    var cellSize: Int = 0

    fun getNullableConnection(zone: Zone): Connection? =
        connections.find { it.z1.index == zone.index || it.z2.index == zone.index }

    fun getRandomEmptyCell(): Cell {
        if (cells.none {
                it.cellType == CellType.EMPTY && !buildings.map { building -> building.getEntrance() }.contains(it)
            }) {
            return Cell(Pair(-1, -1), this)
        }
        return cells.filter {
            it.cellType == CellType.EMPTY && !buildings.map { building -> building.getEntrance() }.contains(it)
        }.random(Constants.rnd)
    }


    override fun toString(): String = "$index, $type"
}

