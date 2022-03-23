package steps

import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.Charsets
import components.*

class TemplateParser {

    suspend fun parse(template: String): List<MutableList<out Any>> {
        var mapInfo = StringBuilder(resourcesVfs[template].readLines(Charsets.UTF8).reduce { acc, s -> acc + s }
            .filter { it != Symbols.WHITESPACE_1.char && it != Symbols.WHITESPACE_2.char })

        // remove comments
        while (mapInfo.contains(Symbols.COMMENT.char)) {
            val start = mapInfo.indexOf(Symbols.COMMENT.char)
            mapInfo = mapInfo.deleteRange(start, mapInfo.indexOf(Symbols.COMMENT.char, startIndex = start + 1) + 1)
        }

        val (connectionsString, zonesAndPlayersString) = mapInfo
            .split(Symbols.ZONE_SEPARATOR.char)
            .filter { it.isNotEmpty() }
            .partition { it.contains(Regex("[-~^]")) }
        val (zonesString, playersString) = zonesAndPlayersString
            .partition { it.first().isDigit() }

        val zones = mutableListOf<Zone>()
        for (i in zonesString.indices)
            zones.add(parseZone(zonesString[i], i))

        val connections = parseConnections(connectionsString[0], zones)

        val players = mutableListOf<Player>()
        for (i in playersString.indices)
            players.add(parsePlayer(playersString[i], zones, i))

        return listOf(zones, connections, players)
    }

    private fun parseZone(zone: String, index: Int): Zone {
        val fields = zone.split(Symbols.SEPARATOR.char)
        return Zone(Surface.valueOf(fields[1].uppercase()), fields[0].toInt(), mutableListOf(), index)
    }

    private fun parsePlayer(player: String, zones: List<Zone>, index: Int): Player {
        val fields = player.split(Symbols.SEPARATOR.char)
        return Player(
            Fraction.valueOf(fields[0].uppercase()),
            index,
            zones[fields[1].toInt()],
            if (fields.size == 3) fields[2].toInt() else 0
        )
    }

    private fun parseConnections(connectionsString: String, zones: List<Zone>): MutableList<Connection> {
        val connections = mutableListOf<Connection>()
        for (i in connectionsString.split(Symbols.SEPARATOR.char)) {
            val z1 = zones[i[0].code - '0'.code]
            val z2 = zones[i[2].code - '0'.code]

            var connectionType = ConnectionType.REGULAR
            for (v in Symbols.values())
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

