package components

import external.Template
import steps.map.`object`.Entrance

/**
 * Connection for matrix steps
 */
class Connection(private val tConnection: Template.TemplateConnection, val z1: Zone, val z2: Zone) {
    val entrances = mutableListOf<Entrance>()
    var resolved: Boolean = false
    val guardLevel: Int
        get() = tConnection.guardLevel.value
    val type: ConnectionType
        get() = tConnection.type
}