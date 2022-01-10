package components

class MatrixMap(val matrix: List<List<Cell>>, val zones: List<Zone>, var walkable: Boolean = false) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }
}