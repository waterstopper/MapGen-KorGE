package external

import kotlinx.serialization.Serializable
import util.RandomBoolean
import util.RandomRange

/**
 * Tweaking generation
 */
@Serializable
class Config(
    // path to export
    val exportPath: String = "exported.hmm",
    val mapSize: Int = 64,

    // spawn wood and ore mines first
    val prioritizeBaseMines: RandomBoolean = RandomBoolean(1),
    // guard level for connections with guardLevel=-2
    val connectionGuardLevel: RandomRange = RandomRange(1, 3),
    // guard level for mines with guardLevel=-2
    val mineGuardLevel: RandomRange = RandomRange(-1, -1),
    // wood and ore guards have decreased guard level
    val decreaseGuardLevelAtBaseMineByOne: RandomBoolean = RandomBoolean(1),

    val spawnHeroAtCastle: RandomBoolean = RandomBoolean(-1),

    val connectTeleportWithRoad: RandomBoolean = RandomBoolean(-1),
    // chance of resource pile, campfire, mana crystal and artifact respectfully
    val treasureChance: List<Double> = listOf(0.72, 0.15, 0.1, 0.03),
    // cost of resource pile, campfire, mana crystal and artifact subtracted from zone richness
    val treasureCost: List<Int> = listOf(1, 2, 3, 5),

    // amount of guards of level 0 to 6
    val guardCount: List<RandomRange> = listOf(
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100)
    ),
    // export maps automatically after generation
    val autoExport: Boolean = true,
    // generate all map
    val generateAll: Boolean = true,
    // add roads to map
    val addRoads: Boolean = true
) {
    init {
        validate(this)
    }

    companion object {
        /**
         * Check that config is correct
         */
        fun validate(config: Config) {
            if (!listOf(32, 64, 128, 256).contains(config.mapSize))
                throw IllegalArgumentException("Map size can be either 32, 64, 128 or 256. Got ${config.mapSize}")

            val guardLevels = listOf(config.mineGuardLevel, config.connectionGuardLevel)
            for (level in guardLevels)
                if (level.value !in (-1..6))
                    throw IllegalArgumentException("Guard level can be in range -1..6. Got ${level.value}")

            if (config.treasureCost.size != 4)
                throw IllegalArgumentException("Treasure cost param should contain 4 elements. Got ${config.treasureCost.size}")
            if (config.treasureChance.size != 4)
                throw IllegalArgumentException("Treasure chance param should contain 4 elements. Got ${config.treasureChance.size}")
            if (config.guardCount.size != 7)
                throw IllegalArgumentException("Guard count param should contain 7 elements. Got ${config.guardCount.size}")
            for (level in config.guardCount)
                if (level.value < 0)
                    throw IllegalArgumentException("Guard count is negative")
        }
    }
}

