package components

import com.soywiz.kds.Array2

class MatrixMap(val matrix: Array2<Cell>, val zones: List<Zone>) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }

    fun isInside(cell: Cell): Boolean {
        return cell.position.first >= 0 &&
                cell.position.first < matrix.width &&
                cell.position.second >= 0 &&
                cell.position.second <= matrix.height
    }

    fun isInside(coords: Pair<Int,Int>): Boolean {
        return coords.first >= 0 &&
                coords.first < matrix.width &&
                coords.second >= 0 &&
                coords.second < matrix.height
    }
}