import com.soywiz.korev.Key
import com.soywiz.korev.addEventListener
import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onDown
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
    var (zones, connections) = t.parse("mapDense.txt")
    zones = zones as MutableList<Zone>
    connections = connections as MutableList<Connection>

    // var iter = 0
    val circles = Container()
    val lines = Container()
    stage.addChildren(listOf(circles, lines))

    //var r = Voronoi.Rect()
    //var r = VoronoiDiagramTask()

    val circ = Circles()

    val matrixLength = 42
    var obstacleMapManager: ObstacleMapManager? = null
    var voronoi: Voronoi? = null

    this.onClick {

        if (!it.isCtrlDown) {
            circ.placeZoneCircles(zones, connections, circles, lines)



            voronoi = Voronoi(zones, matrixLength)
            obstacleMapManager = ObstacleMapManager(voronoi!!.matrixMap)
            voronoi!!.createPassages()


            val mapImage = voronoi!!.visualizeMatrix()

            //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
            lines.image(mapImage.scaleLinear(width / matrixLength, height / matrixLength))
        } else {
            println("Ctrl")
            obstacleMapManager?.connectRegions()
            //obstacleMapManager.connectRegions()

            val mapImage = voronoi?.visualizeMatrix()
            if (mapImage != null) {
                println("good")
                lines.image(mapImage.scaleLinear(width / matrixLength, height / matrixLength))
            }
        }
    }
//    this.onClick {
//        circ.placeZoneCircles(zones, connections, circles, lines, iter)
//        iter++
//    }
}

