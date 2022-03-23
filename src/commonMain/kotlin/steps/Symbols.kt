package steps

import components.ConnectionType

enum class Symbols(var char: Char) {
    REGULAR('-'),
    ROAD('~'),
    PORTAL('^'),
    COMMENT('#'),
    SEPARATOR(','),
    WHITESPACE_1(' '),
    WHITESPACE_2('\n'),
    ZONE_SEPARATOR(';');

    fun convertToConnectionType(): ConnectionType {
        return when (this) {
            REGULAR -> ConnectionType.REGULAR
            ROAD -> ConnectionType.ROAD
            PORTAL -> ConnectionType.PORTAL
            else -> throw Error("No such type")
        }
    }
}