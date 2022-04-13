import Constants.matrixMap
import Constants.zones
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeFormat
import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.Stage
import com.soywiz.korim.color.Colors
import com.soywiz.korio.lang.Environment
import components.ConnectionType
import components.Surface
import external.Config
import external.FileReader.readConfig
import external.Template
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import steps.Guards
import steps.Pipeline
import steps.map.`object`.BuildingsManager
import steps.obstacle.ObstacleMapManager
import steps.passage.GridPassage
import steps.passage.RoadBuilder
import steps.voronoi.Voronoi

const val height = 640
const val width = 640

suspend fun main() = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    println(DateTime.now().time.format(TimeFormat.FORMAT_TIME))
    println(DateTime.now().time.format(TimeFormat.DEFAULT_FORMAT))
    createTemplate()
    println(Json.encodeToString(Config()))
    Constants.config = readConfig(Environment["config"] ?: "src/commonMain/resources/config.json")

    println(Environment.getAll())

    var generationStep = 0
    var pipeline = Pipeline.create(stage, width)

    if (Constants.config.generateAll)
        this.onClick {
            //generateAll(stage, pipeline)
            pipeline.createMap()
        }
    else
        this.onClick {
            val (newPipeline, newGenerationStep) = generateSequentially(stage, generationStep, pipeline)
            pipeline = newPipeline
            generationStep = newGenerationStep
        }
    this.keys {
        down(Key.E) {
            pipeline.exportMap()
        }
    }
}

suspend fun generateAll(stage: Stage, pipeline: Pipeline) {
    var pipeline = pipeline
    stage.children.clear()
    zones.clear()
    pipeline = Pipeline.create(stage, stage.width)
    pipeline.createPositioning()

    pipeline.voronoi = Voronoi()
    pipeline.voronoi.createMatrixMap(pipeline.circleZones)

    pipeline.buildingsManager = BuildingsManager(zones)

    pipeline.obstacleMapManager = ObstacleMapManager(pipeline.buildingsManager.buildings)

    GridPassage().createPassages()
    pipeline.buildingsManager.placeTeleports()

    pipeline.obstacleMapManager.connectRegions()

    pipeline.roadBuilder = RoadBuilder()
    pipeline.roadBuilder.connectCastles(pipeline.buildingsManager.castles)
    pipeline.roadBuilder.connectGraphs(zones)
    pipeline.roadBuilder.normalizeRoads(matrixMap)

    pipeline.guards = Guards()
    pipeline.guards.placeGuards()
    pipeline.guards.placeTreasures()

    pipeline.visualize()
    pipeline.exportMap()
}

suspend fun generateSequentially(stage: Stage, generationStep: Int, pipeline: Pipeline): Pair<Pipeline, Int> {
    var pipeline = pipeline
    println(generationStep)
    when (generationStep) {
        0 -> {
            stage.children.clear()
            zones.clear()
            pipeline = Pipeline.create(stage, stage.width)
            pipeline.createPositioning()
        }
        1 -> {
            pipeline.voronoi = Voronoi()
            pipeline.voronoi.createMatrixMap(pipeline.circleZones)
        }
        2 -> pipeline.buildingsManager = BuildingsManager(zones)
        3 -> pipeline.obstacleMapManager = ObstacleMapManager(pipeline.buildingsManager.buildings)
        4 -> {
            GridPassage().createPassages()
            pipeline.buildingsManager.placeTeleports()
        }
        5 -> pipeline.obstacleMapManager.connectRegions()
        6 -> {
            pipeline.roadBuilder = RoadBuilder()
            pipeline.roadBuilder.connectCastles(pipeline.buildingsManager.castles)
            pipeline.roadBuilder.connectGraphs(zones)
            pipeline.roadBuilder.normalizeRoads(matrixMap)
        }
        7 -> {
            pipeline.guards = Guards()
            pipeline.guards.placeGuards()
            pipeline.guards.placeTreasures()
        }
        8 -> pipeline.exportMap()
    }
    if (generationStep > 0)
        pipeline.visualize(generationStep > 1)
    return Pair(pipeline, (generationStep + 1) % 9)
}

fun createTemplate() {
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
        Template(
            "twoLayers",
            listOf(z0, z1, z2, z3, z4, z5, z6, z7, z8),
            listOf(c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10)
        )
    println(Json.encodeToString(template))
}

