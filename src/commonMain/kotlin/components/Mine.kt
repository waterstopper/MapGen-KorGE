package components

class Mine(val resource: Resource, val surface: Surface, position: Pair<Int, Int>, player: Player?) :
    Building(position, player) {
    override fun toString(): String {
        if (resource == Resource.MERCURY)
            return "mercury_lab"
        else if (resource == Resource.ORE)
            return "ore_mine"
        else if (resource == Resource.WOOD)
            return "wood_sawmill${
                if (surface.isSawmillSurface())
                    if (surface == Surface.DESERT || surface == Surface.SAND || surface == Surface.NDESERT) "_beach"
                    else "_${surface.name.lowercase()}" else ""
            }"
        return "${resource.name.lowercase()}_mine${if (surface.isMineSurface()) "_${surface.name.lowercase()}" else ""}"
    }
}