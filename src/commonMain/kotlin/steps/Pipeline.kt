package steps

import Constants
import Constants.config
import Constants.matrixMap
import Constants.zones
import steps.map.`object`.Castle
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.image
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import components.*
import external.FileReader
import external.Writer
import steps.map.`object`.BuildingsManager
import steps.map.`object`.Mine
import steps.map.`object`.Teleport
import steps.obstacle.ObstacleMapManager
import steps.passage.GridPassage
import steps.passage.RoadBuilder
import steps.posititioning.CircleZone
import steps.posititioning.Circles
import steps.posititioning.LineConnection
import steps.voronoi.Voronoi

class Pipeline(
    val circleZones: MutableList<CircleZone>,
    val connections: List<LineConnection>,
    private val stage: Stage? = null,
    private val imageLength: Double = 0.0,
) {
    lateinit var voronoi: Voronoi
    lateinit var obstacleMapManager: ObstacleMapManager
    lateinit var buildingsManager: BuildingsManager
    lateinit var roadBuilder: RoadBuilder
    lateinit var guards: Guards
    lateinit var image: Container

    companion object {
        suspend fun create(stage: Stage?, imageLength: Double): Pipeline {
            val (zones, connections) = FileReader.createZonesAndConnections()
            return Pipeline(
                zones as MutableList<CircleZone>,
                connections as MutableList<LineConnection>,
                stage,
                imageLength
            )
        }
    }

    fun createPositioning() {
        val circles = Container()
        val lines = Container()
        image = lines
        stage!!.addChildren(listOf(circles, lines))

        val circ = Circles()

        circ.placeZoneCircles(circleZones, connections, circles, lines)
    }

    suspend fun createMap() {
        stage!!.children.clear()
        zones.clear()
        createPositioning()

        voronoi = Voronoi()
        voronoi.createMatrixMap(circleZones)

        buildingsManager = BuildingsManager(zones)

        obstacleMapManager = ObstacleMapManager(buildingsManager.buildings)

        GridPassage().createPassages()
        buildingsManager.placeTeleports()

        obstacleMapManager.connectRegions()

        roadBuilder = RoadBuilder()
        roadBuilder.connectCastles(buildingsManager.castles)
        roadBuilder.connectGraphs(zones)
        roadBuilder.normalizeRoads(matrixMap)

        guards = Guards()
        guards.placeGuards()
        guards.placeTreasures()

        visualize()
        if (config.autoExport)
            exportMap()
    }

    fun visualize(showBuildings: Boolean = true) {
        val mapImage = visualizeMatrix(showBuildings)

        image.image(
            mapImage.scaleLinear(
                imageLength / Constants.config.mapSize,
                imageLength / Constants.config.mapSize
            )
        )
    }

    suspend fun exportMap() {
        val w = Writer(obstacleMapManager, buildingsManager, guards)
        w.writeHeader()
        println("exported as ${w.getPath()}")
    }


    /**
     * for debugging mainly
     * yellow - centers of zones
     * blue - roads, passages
     * gray - mines, castles
     */
    fun visualizeMatrix(showBuildings: Boolean = true): Bitmap32 {
        val res = Bitmap32(Constants.config.mapSize, Constants.config.mapSize)
        for (x in 0 until Constants.config.mapSize)
            for (y in 0 until Constants.config.mapSize)
                if (matrixMap.matrix[x, y].cellType == CellType.ROAD)
                    res[x, y] = Colors.BLUE
                else if (Constants.OBSTACLES.contains(matrixMap.matrix[x, y].cellType)) {
                    res[x, y] = matrixMap.matrix[x, y].zone.type.color.minus(RGBA(0x2A2A2A))
                    if (matrixMap.matrix[x, y].cellType == CellType.EDGE)
                        res[x, y] = Colors.PINK
                } else if (matrixMap.matrix[x, y].cellType == CellType.GUARD) {
                    res[x, y] = Colors.PURPLE
                } else if (matrixMap.matrix[x, y].cellType == CellType.TREASURE)
                    res[x, y] = Colors.CORAL
                else res[x, y] = matrixMap.matrix[x, y].zone.type.color

        if (showBuildings)
            for (c in matrixMap.zones) {
                res[c.centerCell.position.first, c.centerCell.position.second] = Colors.YELLOW
//            for (cell in getCellsAtRadius(5, matrixMap.matrix[c.center.first, c.center.second]))
//                res[cell.position.first, cell.position.second] = Colors.ALICEBLUE
//                val interval = MatrixExtensions.getMaxMinRadius(c.centerCell)
//                val cells = MatrixExtensions.getCellsInInterval(
//                    c.centerCell,
//                    0,
//                    interval.second / 2
//                )
//                if (c.castles.size != 0 || c.mines.size != 0)
//                    for (i in cells)
//                        res[i.position.first, i.position.second] = Colors.ALICEBLUE
                res[c.centerCell.position.first, c.centerCell.position.second] = Colors.YELLOW

            }
        if (showBuildings) {
            for (building in buildingsManager.buildings)
                for (cell in building.getCells())
                    res[cell.position.first, cell.position.second] = when (building) {
                        is Castle, is Mine -> Constants.playerColors[building.playerColor + 1]
                        is Teleport -> Constants.playerColors[building.index]
                        else -> Colors.LIME
                    }

            for (c in matrixMap.zones) {
                val vertices =
                    (c.castles.map { it.getEntrance() } + c.mines.map { it.getEntrance() })
                for (i in vertices)
                    res[i.position.first, i.position.second] = Colors.MEDIUMSEAGREEN
            }
        }
        return res
    }
}