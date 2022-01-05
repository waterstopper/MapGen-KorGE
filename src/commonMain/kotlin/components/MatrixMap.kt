package components

class MatrixMap(val matrix: List<List<Cell>>) {
    override fun toString(): String {
        return matrix.joinToString("\n")
    }
}