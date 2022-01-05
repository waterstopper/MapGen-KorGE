import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import components.Connection
import components.Zone
import steps.Circles
import steps.TemplateParser
import steps.Voronoi

const val height = 320
const val width = 320

suspend fun main() = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    val t = TemplateParser()
    var (zones, connections) = t.parse("mapTwoLayers.txt")
    zones = zones as MutableList<Zone>
    connections = connections as MutableList<Connection>

    var iter = 0
    val circles = Container()
    val lines = Container()
    stage.addChildren(listOf(circles, lines))

    //var r = Voronoi.Rect()
    //var r = VoronoiDiagramTask()

    val circ = Circles()
    circ.placeZoneCircles(zones,connections,circles,lines)

    val map = Voronoi().getMatrixMap(zones, 12)

    println(map)

//    this.onClick {
//        circ.placeZoneCircles(zones, connections, circles, lines, iter)
//        iter++
//    }
}

