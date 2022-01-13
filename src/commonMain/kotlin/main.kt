import com.soywiz.korge.Korge
import com.soywiz.korge.resources.resourceBitmap
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.file.std.resourcesVfs
import components.Connection
import components.Zone
import steps.Circles
import steps.ObstacleMapManager
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

    // var iter = 0
    val circles = Container()
    val lines = Container()
    stage.addChildren(listOf(circles, lines))

    //var r = Voronoi.Rect()
    //var r = VoronoiDiagramTask()

    val circ = Circles()
    circ.placeZoneCircles(zones, connections, circles, lines)

    val matrixLength = 42

    val voronoi =  Voronoi(zones, matrixLength)
    ObstacleMapManager(voronoi.matrixMap)

    val mapImage = voronoi.visualizeMatrix()

    //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
    lines.image(mapImage.scaleLinear(width/matrixLength,height/matrixLength))
//    this.onClick {
//        circ.placeZoneCircles(zones, connections, circles, lines, iter)
//        iter++
//    }
}

