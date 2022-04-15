package external

import com.soywiz.korio.file.std.resourcesVfs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.Constants
import com.soywiz.korio.file.std.rootLocalVfs
import steps.posititioning.CircleZone
import steps.posititioning.GraphPart
import steps.posititioning.LineConnection

/**
 * Deserializing config and template
 */
object FileReader {
    suspend fun readConfig(path: String? = null): Config {
        val file = if (path == null) resourcesVfs["config.json"] else rootLocalVfs[path]
        println(file.toString())
        return Json.decodeFromString(file.readString())
    }

    private suspend fun readTemplate(path: String? = null): Template {
        val file = if (path == null) resourcesVfs["template.json"] else rootLocalVfs[path]
        return Json.decodeFromString(file.readString())
    }

    suspend fun createZonesAndConnections(): List<MutableList<out GraphPart>> {
        Constants.template = readTemplate(Constants.templatePath)

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