package components

import com.soywiz.kds.Array2

class MatrixMap(val matrix: Array2<Cell>, val zones: List<Zone>) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }
}