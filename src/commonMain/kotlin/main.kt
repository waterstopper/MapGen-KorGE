import util.Constants.matrixMap
import util.Constants.zones
import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.Stage
import com.soywiz.korim.color.Colors
import external.FileReader.readConfig
import steps.Guards
import steps.Pipeline
import steps.map.`object`.BuildingsManager
import steps.obstacle.ObstacleMapManager
import steps.passage.GridPassage
import steps.passage.RoadBuilder
import steps.voronoi.Voronoi
import util.Constants
import util.Constants.config

const val height = 640
const val width = 640

suspend fun main(args: Array<String>) = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    Constants.templatePath = if (args.isNotEmpty()) args[0] else null
    config = readConfig(if (args.size > 1) args[1] else null)

    var generationStep = 0
    var pipeline = Pipeline.create(stage, width)

    if (config.generateAll)
        this.onClick {
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
            if (config.addRoads) {
                pipeline.roadBuilder = RoadBuilder()
                pipeline.roadBuilder.connectCastles(pipeline.buildingsManager.castles)
                pipeline.roadBuilder.connectGraphs(zones)
                pipeline.roadBuilder.normalizeRoads(matrixMap)
            }
        }
        7 -> {
            pipeline.guards = Guards()
            pipeline.guards.placeGuards()
            pipeline.guards.placeTreasures()
        }
        8 -> {
            if (config.autoExport)
                pipeline.exportMap()
        }
    }
    if (generationStep > 0)
        pipeline.visualize(generationStep > 1)
    return Pair(pipeline, (generationStep + 1) % 9)
}