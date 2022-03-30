package components

import steps.building.Castle
import steps.building.Mine
import external.Template
import steps.posititioning.LineConnection

/**
 * Sizes are computed proportionally to each other.
 * components.Zone placement starts with 0 index-zone.
 */
class Zone constructor(

    private val tZone: Template.TemplateZone,
    var center: Pair<Int, Int>,
    val connections: MutableList<Connection> = mutableListOf()
) {
    val centerCell: Cell
        get() = matrixMap.matrix[center.first, center.second]
    val mines: MutableList<Mine> = mutableListOf()
    val castles: MutableList<Castle> = mutableListOf()
    val index: Int
        get() = tZone.index
    val type: Surface
        get() = tZone.surface
    val edge = mutableListOf<Cell>()
    lateinit var matrixMap: MatrixMap
    val cells by lazy {
        matrixMap.matrix.filter { it.zone == this }
    }

    // used to see
    var cellSize: Int = 0

    fun getNullableConnection(zone: Zone): Connection? =
        connections.find { it.z1.index == zone.index || it.z2.index == zone.index }

    fun getRandomEmptyCell(): Cell = cells.filter { it.cellType == CellType.EMPTY }.random(Constants.rnd)


    override fun toString(): String = "$index, $type"
}

