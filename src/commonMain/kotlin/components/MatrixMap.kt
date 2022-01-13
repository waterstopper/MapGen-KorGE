package components

class MatrixMap(val matrix: List<List<Cell>>, val zones: List<Zone>) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }
}