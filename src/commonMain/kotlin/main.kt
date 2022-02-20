import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import components.Connection
import components.Zone
import export.Writer
import steps.ObstacleMapManager
import steps.Pipeline
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


    val matrixLength = 64
    var obstacleMapManager: ObstacleMapManager? = null
    var voronoi: Voronoi? = null

    var pipeline = Pipeline(zones, connections, matrixLength, stage, width)

    pipeline.exportMap()

    this.onClick {
        pipeline = Pipeline(zones, connections, matrixLength, stage, width)
    }


//    this.onClick {
//        val circles = Container()
//        val lines = Container()
//        stage.addChildren(listOf(circles, lines))
//
//        val circ = Circles()
//
//        circ.placeZoneCircles(zones, connections, circles, lines)
//        println(circles.children.size)
//
//        voronoi = Voronoi(zones, matrixLength)
//        obstacleMapManager = ObstacleMapManager(voronoi!!.matrixMap)
//        voronoi!!.createPassages()
//
//        obstacleMapManager!!.connectRegions()
//
//        val mapImage = voronoi!!.visualizeMatrix()
//
//        //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
//        lines.image(mapImage.scaleLinear(width / matrixLength, height / matrixLength))
//    }

//    this.onClick {
//
//        if (!it.isCtrlDown) {
//            circ.placeZoneCircles(zones, connections, circles, lines)
//
//            voronoi = Voronoi(zones, matrixLength)
//            obstacleMapManager = ObstacleMapManager(voronoi!!.matrixMap)
//            voronoi!!.createPassages()
//
//
//            val mapImage = voronoi!!.visualizeMatrix()
//
//            //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
//            lines.image(mapImage.scaleLinear(width / matrixLength, height / matrixLength))
//        } else {
//
//            obstacleMapManager?.connectRegions()
//            //obstacleMapManager.connectRegions()
//
//            val mapImage = voronoi?.visualizeMatrix()
//            if (mapImage != null) {
//                lines.image(mapImage.scaleLinear(width / matrixLength, height / matrixLength))
//            }
//        }
//    }
}

