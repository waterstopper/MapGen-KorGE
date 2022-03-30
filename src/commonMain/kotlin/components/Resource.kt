package components

import Constants

enum class Resource {
    GOLD,
    ORE,
    WOOD,
    MERCURY,
    GEMS,
    CRYSTALS,
    SULFUR,
    RANDOM;

    companion object {
        fun randomSpecial(): Resource =
            values().filter { it != GOLD && it != ORE && it != WOOD }.random(Constants.rnd)


        fun getRandomExcept(list: List<Resource>) =
            values().filter { !list.contains(it) && it != RANDOM }.random(Constants.rnd)

        fun baseResources() = listOf(ORE, WOOD)

        fun getRandomBaseExcept(list: List<Resource>) =
            baseResources().filter { !list.contains(it) && it != RANDOM }.random(Constants.rnd)
    }
}