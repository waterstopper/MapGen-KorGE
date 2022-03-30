import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korim.color.Colors
import components.ConnectionType
import components.Surface
import external.Template
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import steps.Pipeline

const val height = 320
const val width = 320

suspend fun main() = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    val castle0 = Template.TemplateCastle()
    val castle1 = Template.TemplateCastle()
    val castle2 = Template.TemplateCastle()

    val z0 = Template.TemplateZone(Surface.GRASS, 50, 0)
    z0.castles.add(castle0)

    val z1 = Template.TemplateZone(Surface.SNOW, 50, 1)
    z1.castles.add(castle1)
    val z2 = Template.TemplateZone(Surface.LAVA, 50, 2)
    z2.castles.add(castle2)
    val z3 = Template.TemplateZone(Surface.DIRT, 50, 3)

    val z4 = Template.TemplateZone(Surface.SWAMP, 20, 4)
    val z5 = Template.TemplateZone(Surface.NDESERT, 20, 5)
    val z6 = Template.TemplateZone(Surface.WASTELAND, 30, 6)
    val z7 = Template.TemplateZone(Surface.DESERT, 30, 7)
    val z8 = Template.TemplateZone(Surface.LAVA, 30, 8)

    val c0 = Template.TemplateConnection(0, 1)
    val c1 = Template.TemplateConnection(0, 2)
    val c2 = Template.TemplateConnection(0, 3)
    val c3 = Template.TemplateConnection(1, 2)
    val c4 = Template.TemplateConnection(1, 3, ConnectionType.REGULAR)
    val c5 = Template.TemplateConnection(2, 3, ConnectionType.REGULAR)
    val c6 = Template.TemplateConnection(1, 4, ConnectionType.REGULAR)
    val c7 = Template.TemplateConnection(2, 5, ConnectionType.REGULAR)
    val c8 = Template.TemplateConnection(3, 6, ConnectionType.REGULAR)
    val c9 = Template.TemplateConnection(3, 7, ConnectionType.REGULAR)
    val c10 = Template.TemplateConnection(3, 8, ConnectionType.REGULAR)
    val template =
        Template("twoLayers",listOf(z0, z1, z2, z3, z4, z5, z6, z7, z8), listOf(c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10))
    println(Json.encodeToString(template))

   val pipeline = Pipeline(stage, width)

//    // val t = Tem plateParser()
//    var (zones, connections) = FileReader.createZonesAndConnections()
//    zones = zones as MutableList<CircleZone>
//    connections = connections as MutableList<LineConnection>
//
//    // var iter = 0
//
//
//    val matrixLength = 64
//    var obstacleMapManager: ObstacleMapManager? = null
//    var voronoi: Voronoi? = null
//
//    var pipeline = Pipeline(zones, connections, stage, width)
//
//    pipeline.exportMap()

    this.onClick {
        val pipeline = Pipeline(stage, width)
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
//
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

