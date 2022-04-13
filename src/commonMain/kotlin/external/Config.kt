package external

import kotlinx.serialization.Serializable

/**
 * @param mapSize
 */
@Serializable
class Config(
    val templatePath: String = "template.json",
    val exportPath: String = "exported.hmm",
    val mapSize: Int = 64,

    val prioritizeBaseMines: RandomBoolean = RandomBoolean(1),

    val connectionGuardLevel: RandomRange = RandomRange(1, 3),
    val mineGuardLevel: RandomRange = RandomRange(0, 2),
    val decreaseGuardLevelAtBaseMineByOne: RandomBoolean = RandomBoolean(1),
    val guardAtNeutralCastle: RandomBoolean = RandomBoolean(0),
    val castleGuardLevel: RandomRange = RandomRange(0, 0),

    val spawnHeroAtCastle: RandomBoolean = RandomBoolean(-1),

    val connectTeleportWithRoad: RandomBoolean = RandomBoolean(1),

    val treasureChance: List<Double> = listOf(0.72, 0.15, 0.1, 0.03),
    val treasureCost: List<Int> = listOf(1, 2, 3, 5),

    val guardCount: List<RandomRange> = listOf(
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100),
        RandomRange(1, 100)
    ),

    val autoExport: Boolean = true,
    val generateAll: Boolean = false
) {
    init {
        validate(this)
    }

    companion object {
        fun validate(config: Config) {
            if (!listOf(32, 64, 128, 256).contains(config.mapSize))
                throw IllegalArgumentException("Map size can be either 32, 64, 128 or 256. Got ${config.mapSize}")

            val guardLevels = listOf(config.castleGuardLevel, config.mineGuardLevel, config.connectionGuardLevel)
            for (level in guardLevels)
                if (level.value !in (0..6))
                    throw IllegalArgumentException("Guard level can be in range 0..6. Got ${level.value}")

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

