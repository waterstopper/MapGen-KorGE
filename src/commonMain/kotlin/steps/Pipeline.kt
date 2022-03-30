package steps

import Constants
import MatrixExtensions.getCellsAtRadius
import steps.building.Castle
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.image
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import components.*
import external.FileReader
import steps.building.BuildingsManager
import steps.building.Mine
import steps.building.Teleport
import steps.obstacle.ObstacleMapManager
import steps.passage.GridPassage
import steps.posititioning.CircleZone
import steps.posititioning.Circles
import steps.posititioning.LineConnection
import steps.voronoi.Voronoi

class Pipeline(
    private val circleZones: MutableList<CircleZone>,
    val connections: List<LineConnection>,
    private val stage: Stage? = null,
    private val imageLength: Double = 0.0,
) {
    lateinit var obstacleMapManager: ObstacleMapManager
    private lateinit var buildingsManager: BuildingsManager
    lateinit var matrixMap: MatrixMap
    lateinit var zones: List<Zone>

    companion object {
        suspend operator fun invoke(stage: Stage?, imageLength: Double): Pipeline {
            val (zones, connections) = FileReader.createZonesAndConnections()
            return Pipeline(
                zones as MutableList<CircleZone>,
                connections as MutableList<LineConnection>,
                stage,
                imageLength
            )
        }
    }

    init {
        createMap()
    }

    private fun createMap() {
        val circles = Container()
        val lines = Container()
        stage!!.addChildren(listOf(circles, lines))

        val circ = Circles()

        circ.placeZoneCircles(circleZones, connections, circles, lines)

        Voronoi.createMatrixMap(circleZones)

        matrixMap = Voronoi.matrixMap
        zones = Voronoi.zones

        buildingsManager = BuildingsManager(matrixMap, zones)

        obstacleMapManager = ObstacleMapManager(matrixMap, buildingsManager.buildings)

        // could not create passages -> retry from start
        if (!GridPassage.createPassages())
            createMap()

        buildingsManager.placeTeleports()

        obstacleMapManager.connectRegions()

        val mapImage = visualizeMatrix()

        //mapImage.updateColors { it.minus(RGBA(0,0,0,100)) }
        lines.image(
            mapImage.scaleLinear(
                imageLength / Constants.config.mapSize,
                imageLength / Constants.config.mapSize
            )
        )
    }

    suspend fun exportMap() {
        //val w = Writer(voronoi!!, obstacleMapManager, buildingsManager)
        // w.writeHeader()
    }


    /**
     * for debugging mainly
     * yellow - centers of zones
     * blue - roads, passages
     * gray - mines, castles
     */
    fun visualizeMatrix(): Bitmap32 {
        val res = Bitmap32(Constants.config.mapSize, Constants.config.mapSize)
        for (x in 0 until Constants.config.mapSize)
            for (y in 0 until Constants.config.mapSize)
                if (matrixMap.matrix[x, y].cellType == CellType.ROAD)
                    res[x, y] = Colors.BLUE
                else if (Constants.OBSTACLES.contains(matrixMap.matrix[x, y].cellType)) {
                    res[x, y] = matrixMap.matrix[x, y].zone.type.color.minus(RGBA(0x2A2A2A))
                    if (matrixMap.matrix[x, y].cellType == CellType.EDGE)
                        res[x, y] = Colors.PINK
                } else res[x, y] = matrixMap.matrix[x, y].zone.type.color

        for (c in matrixMap.zones) {
            res[c.center.first, c.center.second] = Colors.YELLOW
//            for (cell in getCellsAtRadius(5, matrixMap.matrix[c.center.first, c.center.second]))
//                res[cell.position.first, cell.position.second] = Colors.ALICEBLUE
            val interval = MatrixExtensions.getMaxMinRadius(c.matrixMap.matrix[c.center.first, c.center.second])
            val cells = MatrixExtensions.getCellsInInterval(
                c.matrixMap.matrix[c.center.first, c.center.second],
                interval.first - 2,
                interval.second * 2 / 3
            )
//            for (i in cells)
//                res[i.position.first, i.position.second] = Colors.ALICEBLUE
        }
        for (building in buildingsManager.buildings) {
            for (cell in building.getCells())
                res[cell.position.first, cell.position.second] = when (building) {
                    is Castle, is Mine -> Constants.playerColors[building.playerColor + 1]
                    is Teleport -> Constants.playerColors[building.index]
                    else -> Colors.LIME
                }
        }

        return res
    }
}