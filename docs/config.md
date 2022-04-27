# Config

Config is a JSON file (and corresponding [class](../src/commonMain/kotlin/external/Config.kt)) that defines generator's
behaviour.

## Options

`exportPath: String` - path to export

`mapSize: Int` - size of map. 32, 64, 128 or 256

`prioritizeBaseMines: RandomBoolean` - spawn wood and ore mines before other

`connectionGuardLevel: RandomRange` - replaces guard level of connections, where `guardLevel == -2`

`decreaseGuardLevelAtBaseMineByOne: RandomBoolean` - wood and ore mine guards have decreased guard level

`spawnHeroAtCastle: RandomBoolean`

`connectTeleportWithRoad: RandomBoolean`

`treasureChance: List<Double>` - chance of resource pile, campfire, mana crystal and artifact respectfully. Sum of all
four values should be exactly 1.

`treasureCost: List<Int>` - cost of resource pile, campfire, mana crystal and artifact subtracted from zone richness,
when a treasure is added to zone.

`guardCount: List<RandomRange>` - amount of guards of level 0 to 6. 0 for random number (random by PPH).

`autoExport: Boolean` - export maps automatically after generation

`generateAll` - generate all steps by one click

`addRoads: Boolean` - add roads to map

## Creating config file

Easy way: create `Config` instance and serialize it to json with `Json.encodeToString(config)`.

Hard way: change config.json manually.

## Random Range/Boolean

[Random range](../src/commonMain/kotlin/util/RandomRange.kt)
and [random boolean](../src/commonMain/kotlin/util/RandomBoolean.kt) are a way to create random integer and boolean
values during runtime. They are specifically created for config options.