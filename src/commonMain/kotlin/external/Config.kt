package external

import kotlinx.serialization.Serializable

@Serializable
class Config(
    val mapSize: Int = 42,

    val prioritizeBaseMines: RandomBoolean = RandomBoolean(1),
    val freeResourcesAtMineMin: Int = 0,
    val freeResourcesAtMineMax: Int = 2,
    val decreaseGuardLevelAtBaseMineByOne: RandomBoolean = RandomBoolean(1),
    val mineGuardLevelMin: Int = 1,
    val mineGuardLevelMax: Int = 2,

    val guardAtNeutralCastle: RandomBoolean = RandomBoolean(0),
    val castleGuardLevelMax: Int = 3,
    val castleGuardLevelMin: Int = 2,
    val spawnHeroAtCastle: RandomBoolean = RandomBoolean(-1),

    val zoneGuardLevelMin: Int = 2,
    val zoneGuardLevelMax: Int = 3,

    val spawnArtifacts: RandomBoolean = RandomBoolean(1),
    val artifactCost: Int = 5,
    val artifactSpawnChance: Double = 0.03
) {
}

