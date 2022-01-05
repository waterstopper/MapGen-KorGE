package components

class Cell(val index: Int, val surface: Biome) {

    override fun toString(): String {
        return index.toString()
    }
}