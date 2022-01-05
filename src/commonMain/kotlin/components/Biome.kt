package components

enum class Biome(val color: String) {
    RANDOM("#808080"),
    DIRT("#964B00"),
    GRASS("#378805"),
    LAVA("#FF0000"),
    SNOW("#E6E1E1"),
    SWAMP("#5D6A00");

    companion object {
        fun fromInt(value: Int): Biome {
            return when (value) {
                0 -> RANDOM
                1 -> DIRT
                2 -> GRASS
                3 -> LAVA
                4 -> SNOW
                5 -> SWAMP
                else -> throw IllegalArgumentException("No zoneType for that")
            }
        }
    }
}