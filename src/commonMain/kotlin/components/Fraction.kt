package components

import util.Constants.rnd

enum class Fraction {
    CITADEL,
    STRONGHOLD,
    TOWER,
    DUNGEON,
    FORTRESS,
    NECROPOLIS,
    RANDOM;

    companion object {
        fun getRandom() = values().filter { it != RANDOM }.random(rnd)
    }
}