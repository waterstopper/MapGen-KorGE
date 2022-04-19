import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.Stage
import com.soywiz.korim.color.Colors
import components.Surface
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
import util.Constants
import util.Constants.config
import util.Constants.matrixMap
import util.Constants.zones

const val height = 640
const val width = 640


suspend fun main(args: Array<String>) = Korge(
    width = width, height = height, bgcolor = Colors["#111111"]
) {
    createTemplate()
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

fun createTemplate() {
    val z0 = Template.TemplateZone(Surface.SAND, 50, 0)
    val z1 = Template.TemplateZone(Surface.SNOW, 50, 1)
    val z2 = Template.TemplateZone(Surface.LAVA, 50, 2)
    val z3 = Template.TemplateZone(Surface.SWAMP, 50, 3)
    val z4 = Template.TemplateZone(Surface.DIRT, 50, 4)
    val z5 = Template.TemplateZone(Surface.WASTELAND, 50, 5)
    val z6 = Template.TemplateZone(Surface.NDESERT, 50, 6)
    val z7 = Template.TemplateZone(Surface.GRASS, 50, 7)
    val z8 = Template.TemplateZone(Surface.SAND, 50, 8)

    val z9 = Template.TemplateZone(Surface.SAND, 50, 9)
    val z10 = Template.TemplateZone(Surface.SNOW, 50, 10)
    val z11 = Template.TemplateZone(Surface.LAVA, 50, 11)
    val z12 = Template.TemplateZone(Surface.SWAMP, 50, 12)
    val z13 = Template.TemplateZone(Surface.SWAMP, 50, 13)
    val z14 = Template.TemplateZone(Surface.DIRT, 50, 14)
    val z15 = Template.TemplateZone(Surface.WASTELAND, 50, 15)
    val z16 = Template.TemplateZone(Surface.NDESERT, 50, 16)
    val z17 = Template.TemplateZone(Surface.GRASS, 50, 17)
    val z18 = Template.TemplateZone(Surface.SAND, 50, 18)
    val z19 = Template.TemplateZone(Surface.DIRT, 50, 19)
    val z20 = Template.TemplateZone(Surface.WASTELAND, 50, 20)
    val z21 = Template.TemplateZone(Surface.NDESERT, 50, 21)
    val z22 = Template.TemplateZone(Surface.GRASS, 50, 22)
    val z23 = Template.TemplateZone(Surface.SAND, 50, 23)
    val z24 = Template.TemplateZone(Surface.SAND, 50, 24)

    val c01 = Template.TemplateConnection(0, 1)
    val c02 = Template.TemplateConnection(0, 2)
    val c03 = Template.TemplateConnection(0, 3)
    val c04 = Template.TemplateConnection(0, 4)
    val c15 = Template.TemplateConnection(1, 5)
    val c25 = Template.TemplateConnection(2, 5)
    val c26 = Template.TemplateConnection(2, 6)
    val c36 = Template.TemplateConnection(3, 6)
    val c37 = Template.TemplateConnection(3, 7)
    val c47 = Template.TemplateConnection(4, 7)
    val c48 = Template.TemplateConnection(4, 8)
    val c18 = Template.TemplateConnection(1, 8)

    val c19 = Template.TemplateConnection(1, 9)
    val c510 = Template.TemplateConnection(5, 10)
    val c512 = Template.TemplateConnection(5, 12)
    val c213 = Template.TemplateConnection(2, 13)
    val c614 = Template.TemplateConnection(6, 14)
    val c616 = Template.TemplateConnection(6, 16)
    val c317 = Template.TemplateConnection(3, 17)
    val c718 = Template.TemplateConnection(7, 18)
    val c720 = Template.TemplateConnection(7, 20)
    val c421 = Template.TemplateConnection(4, 21)
    val c822 = Template.TemplateConnection(8, 22)
    val c824 = Template.TemplateConnection(8, 24)

    val c910 = Template.TemplateConnection(9, 10)
    val c1011 = Template.TemplateConnection(10, 11)
    val c1112 = Template.TemplateConnection(11, 12)
    val c1213 = Template.TemplateConnection(12, 13)
    val c1314 = Template.TemplateConnection(13, 14)
    val c1415 = Template.TemplateConnection(14, 15)
    val c1516 = Template.TemplateConnection(15, 16)
    val c1617 = Template.TemplateConnection(16, 17)
    val c1718 = Template.TemplateConnection(17, 18)
    val c1819 = Template.TemplateConnection(18, 19)
    val c1920 = Template.TemplateConnection(19, 20)
    val c2021 = Template.TemplateConnection(20, 21)
    val c2122 = Template.TemplateConnection(21, 22)
    val c2223 = Template.TemplateConnection(22, 23)
    val c2324 = Template.TemplateConnection(23, 24)
    val c924 = Template.TemplateConnection(9, 24)

    val template = Template(
        "big",
        listOf(
            z0, z1, z2, z3, z4, z5, z6, z7, z8, z9, z10, z11,
            z12, z13, z14, z15, z16, z17, z18, z19, z20, z21, z22, z23, z24
        ),
        listOf(
            c01, c02, c03, c04, c15, c25, c26, c36, c37, c47, c48, c18, c19, c510, c512,
            c213, c614, c616, c317, c718, c720, c421, c822, c824, c910, c1011, c1112, c1213, c1314, c1415,
            c1516, c1617, c1718, c1819, c1920, c2021, c2122, c2223, c2324, c924
        )
    )
    //val template = Template("one",listOf(z0),listOf())
    println(Json.encodeToString(template))
}