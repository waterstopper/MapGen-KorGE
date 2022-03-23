package components

class Player(val fraction: Fraction, val color:Int, val zone: Zone, val heroes: Int) {
    lateinit var castle: Castle
    override fun toString(): String {
        return "$fraction, $zone; $heroes heroes"
    }
}