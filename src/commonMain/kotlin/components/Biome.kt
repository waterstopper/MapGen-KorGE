package components

import com.soywiz.korim.color.RGBA

enum class Biome(val color: RGBA) {
    RANDOM(RGBA(250,250,250)),
    WATER(RGBA(8,28,128)),
    SAND(RGBA(214,182,148)),
    DIRT(RGBA(99,48,8)),
    GRASS(RGBA(24, 91,16)),
    SWAMP(RGBA(0,44,0)),
    LAVA(RGBA(48,48,48)),
    WASTELAND(RGBA(165,85,16)),
    DESERT(RGBA(181,138,24)),
    SNOW(RGBA(220,220,220)),
    NEW_DESERT(RGBA(192,160,0)),
    PAVEMENT(RGBA(160,160,160)),
    NEW_WASTELAND(RGBA(192,192,160));

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