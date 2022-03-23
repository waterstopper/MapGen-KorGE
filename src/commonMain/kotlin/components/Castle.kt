package components

class Castle(val fraction: Fraction, player: Player, position: Pair<Int, Int>, val orientation: String) :
    Building(position, player)