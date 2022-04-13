import com.soywiz.korim.color.Colors
import components.CellType
import components.MatrixMap
import components.Zone
import external.Config
import external.FileReader
import external.Template
import steps.voronoi.Voronoi
import kotlin.random.Random

object Constants {
    private const val SEED = 42
    val rnd = Random(SEED)
    var config: Config = Config()
    var template: Template = Template("template", listOf(), listOf())
    lateinit var matrixMap: MatrixMap
    var zones: MutableList<Zone> = mutableListOf()

    val playerColors = listOf(
        Colors.SLATEGRAY, Colors.RED, Colors.GREEN,
        Colors.BLUE, Colors.CYAN, Colors.PURPLE, Colors.YELLOW
    )

    // chance of placing obstacle when randomly placing
    const val RANDOM_OBSTACLE_CHANCE = 0.5

    // chance of placing obstacle on a cellular automata step
    const val RATIO_OBSTACLE_CHANCE = 0.4

    // if neighbor ratio exceeds this value, then a cell is 100% obstacle
    const val OBSTACLE_RATIO = 0.75 // 6/8

    // if neighbor ratio is smaller than this value, a cell is 100% empty
    const val EMPTY_RATIO = 0.5

    const val CELLULAR_AUTOMATA_STEPS = 1

    const val SIDE_COST = 2
    const val EMPTY_COST = 0

    val SUPER_OBSTACLES = listOf(CellType.EDGE, CellType.BUILDING)
    val SUPER_EMPTY = listOf(CellType.ROAD)
    val OBSTACLES = listOf(CellType.EDGE, CellType.OBSTACLE)
    val EMPTY = listOf(CellType.EMPTY, CellType.ROAD)

    const val mapEditorVersion = 0x20
}