package components

class MatrixMap(val matrix: List<List<Cell>>, val centers: List<Pair<Int, Int>>) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }
}