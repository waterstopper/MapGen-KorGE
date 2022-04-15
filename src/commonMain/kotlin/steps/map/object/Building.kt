package steps.map.`object`

import components.Cell
import components.CellType

/**
 * Building for map.
 * All toString() methods in inherited classes are for exporting. Changing them will break export
 */
abstract class Building(position: Pair<Int, Int>, val playerColor: Int = -1) :
    MapObject(position) {
    abstract fun getCells(): List<Cell>
    abstract fun getEntrance(): Cell

    open fun place(position: Pair<Int, Int>): Boolean {
        this.position = position
        val res = isValidEntrance() && isValidPosition()
        if (res)
            cellsToBuilding()
        return res
    }

    private fun isValidPosition(): Boolean {
        return !getCells().any {
            it.cellType == CellType.BUILDING
                    || it.cellType == CellType.OBSTACLE
                    || it.cellType == CellType.EDGE
        }
    }

    private fun cellsToBuilding() {
        getCells().forEach { it.cellType = CellType.BUILDING }
        getEntrance().cellType = CellType.ROAD
    }

    private fun isValidEntrance() = !getEntrance().getNeighbors().filter { !getCells().contains(it) }
        .all { it.cellType == CellType.BUILDING || it.cellType == CellType.EDGE || it.cellType == CellType.OBSTACLE }

    override fun toString(): String {
        return "$position"
    }
}