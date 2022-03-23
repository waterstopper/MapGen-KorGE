package components

import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA

enum class Surface(val color: RGBA) {
    WATER(RGBA(8, 28, 128)),
    SAND(RGBA(214, 182, 148)),
    DIRT(RGBA(99, 48, 8)),
    GRASS(RGBA(24, 91, 16)),
    SWAMP(RGBA(0, 44, 0)),
    LAVA(RGBA(48, 48, 48)),
    WASTELAND(RGBA(165, 85, 16)),
    DESERT(RGBA(181, 138, 24)),
    SNOW(RGBA(220, 220, 220)),
    NDESERT(RGBA(192, 160, 0)),
    PAVEMENT(RGBA(160, 160, 160)),
    NWASTELAND(RGBA(192, 192, 160)),
    COUNT(Colors.PINK),
    RANDOM(RGBA(250, 250, 250));

    fun isMineSurface(): Boolean {
        return this == LAVA || this == SWAMP || this == SNOW
    }

    fun isSawmillSurface(): Boolean = this == LAVA || this == DESERT || this == NDESERT || this == SNOW

    companion object {
        fun fromInt(value: Int): Surface {
            return when (value) {
                0 -> WATER//RANDOM TODO figure out where it is used
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

