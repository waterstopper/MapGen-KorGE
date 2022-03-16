package steps

import components.Biome
import components.Connection
import components.ConnectionType
import components.GraphPart
import components.Zone
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.Charsets

class TemplateParser {

    suspend fun parse(template: String): List<MutableList<out GraphPart>> {
        var mapInfo = StringBuilder(resourcesVfs[template].readLines(Charsets.UTF8).reduce { acc, s -> acc + s }
            .filter { it != SYMBOLS.WHITESPACE_1.char && it != SYMBOLS.WHITESPACE_2.char })

        // remove comments
        while (mapInfo.contains(SYMBOLS.COMMENT.char)) {
            val start = mapInfo.indexOf(SYMBOLS.COMMENT.char)
            mapInfo = mapInfo.deleteRange(start, mapInfo.indexOf(SYMBOLS.COMMENT.char, startIndex = start + 1) + 1)
        }

        val (connectionsString, zonesString) = mapInfo
            .split(SYMBOLS.ZONE_SEPARATOR.char)
            .partition { it.contains(Regex("[-~^]")) }
            .toList()

        val zones = mutableListOf<Zone>()
        for (i in zonesString.indices)
            zones.add(parseZone(zonesString[i], i))

        val connections = parseConnections(connectionsString[0], zones)

        return listOf(zones, connections)
    }

    private fun parseZone(zone: String, index: Int): Zone {
        val fields = zone.split(SYMBOLS.SEPARATOR.char)
        return Zone(Biome.valueOf(fields[1].uppercase()), fields[0].toInt(), mutableListOf(), index)
    }

    private fun parseConnections(connectionsString: String, zones: List<Zone>): MutableList<Connection> {
        val connections = mutableListOf<Connection>()
        for (i in connectionsString.split(SYMBOLS.SEPARATOR.char)) {
            val z1 = zones[i[0].code - '0'.code]
            val z2 = zones[i[2].code - '0'.code]

            var connectionType = ConnectionType.REGULAR
            for (v in SYMBOLS.values())
                if (v.char == i[1]) {
                    connectionType = v.convertToConnectionType()
                    break
                }

            connections.add(
                Connection(
                    z1, z2, connectionType, 0
                )
            )

            z1.connections.add(connections.last())
            z2.connections.add(connections.last())
        }

        return connections
    }
}

enum class SYMBOLS(var char: Char) {
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