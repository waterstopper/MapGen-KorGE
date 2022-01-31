package steps

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.image
import components.Connection
import components.Zone

class Pipeline(
    val zones: MutableList<Zone>,
    val connections: List<Connection>,
    val matrixLength: Int,
    val stage: Stage,
    val imageLength: Double
) {
    //val voronoi:Voronoi = Voronoi()


    init {
        createMap()
    }


    private fun createMap() {
        val circles = Container()
        val lines = Container()
        stage.addChildren(listOf(circles, lines))

        val circ = Circles()

        circ.placeZoneCircles(zones, connections, circles, lines)

        val voronoi = Voronoi(zones, matrixLength)
        val obstacleMapManager = ObstacleMapManager(voronoi.matrixMap)

        if (!voronoi.createPassages())
            createMap()

        obstacleMapManager.connectRegions()

        val mapImage = voronoi.visualizeMatrix()

        //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
        lines.image(mapImage.scaleLinear(imageLength / matrixLength, imageLength / matrixLength))
    }

    fun drawMap() {

    }
}