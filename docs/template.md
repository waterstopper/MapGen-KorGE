# Template

Templates are parsed into [Template](../src/commonMain/kotlin/external/Template.kt) class

## Zone

`surface: Surface` - surface of zone.

`size: Int` - relative zone size. Is important in relation to other zone sizes.

`index: Int`- number of a zone. Should be stated explicitly for better understanding of which connections correspond to
which zones.

`richness: Int` - parameter for treasures. States how much summarily cost is in this zone. **TODO: should be
RandomRange**.

Castles and mines either defined in respective lists or can be defined by stating number of castles and mines
in `castlesNum` and `minesNum`. If both methods are used, `minesNum - mines.size` random mines are added to `mines`
list.

## Connection

Each connection is between two zones. Therefore, template resembles a graph.

* Connection contains indices of two connected zones.
* Its type (**road**, **portal** or **regular** - try to add road, if not possible create portal)
* Guard level - value of type `Random range`. If -1, then guard is not placed, if -2 then level is taken from config.

## Mine

`resource: Resource` - type of resource that mine produces.

`guardLevel: RandomRange` - guard level.

`player: Int` - index of player that owns mine. -1 if mine is neutral. **TODO add mine player to export**

## Castle

`fraction: Fraction` - fraction of castle (e.g. Citadel, Necropolis, Tower)

`player: Int` - index of player that owns castle.