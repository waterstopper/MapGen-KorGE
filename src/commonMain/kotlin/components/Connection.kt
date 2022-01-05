package components

import GeometryExtensions.intersects
import GeometryExtensions.points
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Line

/**
 * IMPORTANT:
 * Roads connect only two zones.
 * For a more complex connection create a 0-sized zone, which will act as a junction between zones
 */
class Connection constructor(val z1: Zone, val z2: Zone, val type: ConnectionType, val creatureLevel: Int) : GraphPart {
    lateinit var line: Line

    fun isInitialized() = ::line.isInitialized

    fun getZone(source: Zone): Zone {
        return if (z2 === source) z1 else z2
    }

    fun intersectsAny(lines: Container): Boolean {
        lines.forEachChild { i -> if (line.intersects(i as Line)) return true }
        return false
    }

    fun intersectsList(connections: List<Connection>): List<Connection> {
        return connections.filter { it.isInitialized() && line.intersects(it.line) }
    }

    /**
     * make sure that z1 has x1 y1 coords, z2 has x2 y2 coords for "redraw connections" method.
     */
    fun initializeLine(linit: Line): Line {
        line = linit
        if (line.points()[1] == z1.getCenter())
            line.setPoints(line.points()[1], line.points()[0])
        return line
    }

}

/**
 * It is recommended to use regular roads only - it never gives an error
 *
 * REGULAR: will try to create a road between 2 zones, if not possible a pair of interconnected portals will appear
 * ROAD: an obligatory road. Are resolved first.
 * PORTAL: an obligatory portal. Are resolved last.
 */
enum class ConnectionType {
    ROAD,
    REGULAR,
    PORTAL
}