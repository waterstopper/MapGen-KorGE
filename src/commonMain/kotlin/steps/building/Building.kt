package steps.building

import components.Cell
import components.CellType
import components.MatrixMap

abstract class Building(val map: MatrixMap, var position: Pair<Int, Int> = Pair(-1, -1), val playerColor: Int = -1) {
    abstract fun getCells(): List<Cell>
    abstract fun getEntrance(): Cell
    fun cellsToBuilding() = getCells().forEach { it.cellType = CellType.BUILDING }
}