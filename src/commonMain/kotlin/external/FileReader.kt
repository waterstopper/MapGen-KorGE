package external

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import Constants
import com.soywiz.korio.file.std.rootLocalVfs

import steps.posititioning.CircleZone
import steps.posititioning.GraphPart
import steps.posititioning.LineConnection

object FileReader {
    suspend fun readConfig(path: String = "config.json"): Config {
        val file = rootLocalVfs[path].readString()
        return Json.decodeFromString(file)
    }

    private suspend fun readTemplate(path: String = "template.json"): Template {
        val file = rootLocalVfs[path].readString()
        return Json.decodeFromString(file)
    }

    suspend fun createZonesAndConnections(): List<MutableList<out GraphPart>> {
        Constants.template = readTemplate()

        val zones = mutableListOf<CircleZone>()
        for (tZone in Constants.template.zones) {
            zones.add(CircleZone(tZone))
        }
        val connections = mutableListOf<LineConnection>()
        for (tConnection in Constants.template.connections) {
            connections.add(LineConnection(zones[tConnection.first], zones[tConnection.second], tConnection.type))
            zones[connections.last().z1.index].connections.add(connections.last())
            zones[connections.last().z2.index].connections.add(connections.last())
        }

        return listOf(zones, connections)
    }

}