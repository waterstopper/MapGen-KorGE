package steps

import com.soywiz.kds.PriorityQueue
import components.Cell
import components.CellType
import components.MatrixMap
import components.Zone
import kotlin.math.max
import kotlin.random.Random

class ObstacleMapManager(private val matrixMap: MatrixMap) {
    var mininum = Int.MAX_VALUE - 2 * max(Constants.SIDE_COST, Constants.DIAG_COST)
    val minCells = mutableListOf<Cell>()

    init {
        randomizeObstacles()
        for (i in 0 until Constants.CELLULAR_AUTOMATA_STEPS)
            cellularAutomataStep()

    }


    /**
     * place random obstacles throughout the map
     */
    private fun randomizeObstacles() {
        for (list in matrixMap.matrix)
            for (cell in list)
                if (!Constants.SUPER_EMPTY.contains(cell.cellType)
                    && !Constants.SUPER_OBSTACLES.contains(cell.cellType)
                    && Random.nextFloat() <= Constants.RANDOM_OBSTACLE_CHANCE
                )
                    cell.cellType = CellType.OBSTACLE
    }

    /**
     * assign obstacle to cell if its neighbors are obstacles
     */
    private fun cellularAutomataStep() {
        for (list in matrixMap.matrix)
            for (cell in list) {
                if (!Constants.SUPER_EMPTY.contains(cell.cellType)
                    && !Constants.SUPER_OBSTACLES.contains(cell.cellType)
                ) {
                    val ratio = cell.getObstacleNeighborsRatio()
                    // make obstacle
                    if (ratio >= Constants.OBSTACLE_RATIO)
                        cell.cellType = CellType.OBSTACLE
                    // make empty
                    else if (ratio <= Constants.EMPTY_RATIO)
                        cell.cellType = CellType.EMPTY
                    // decide by chance
                    else if (Random.nextFloat() > Constants.RATIO_OBSTACLE_CHANCE)
                        cell.cellType = CellType.OBSTACLE
                }
            }
    }

    /*
    connect regions that have been isolated because of cellular automata steps
    1) find the main area
    2) calculate costs of moving to another open areas
    3) cut through to the cheapest open area
    4) repeat 2-3 until all is reached
    - - - calculating cost - - -
    if
    1) cell is empty
    2) cost to move there is minimal
    3) and cost > 0
    => it is a needed cell
     */
    fun connectRegions() {
        for (zone in matrixMap.zones) {
            val root = findRoot(zone)
            do {
                val res = calculateCosts(root)
            } while (res)
        }
    }

    /**
     * TODO: how to make it so that second return is not required?
     * Root should be in zone and all its neighbors should be in zone too
     */
    private fun findRoot(zone: Zone): Cell {
        matrixMap.matrix.forEach {
            it.forEach { cell ->
                if (Constants.EMPTY.contains(cell.cellType)
                    && cell.zone == zone
                    && cell.getNeighbors().all { neighbor -> neighbor.zone == zone }
                ) return cell
            }
        }
        return matrixMap.matrix[0][0]
    }

    /**
     * TODO: I don't calculate diagonal costs for now
     * https://www.redblobgames.com/pathfinding/a-star/introduction.html
     * this is where i took an algorithm from
     */
    private fun calculateCosts(root: Cell): Boolean {
        val queue = PriorityQueue<Pair<Int, Cell>>(
            comparator = Comparator { a, b -> a.first.compareTo(b.first) })
        val previous = HashMap<Cell, Cell>()
        val cost = HashMap<Cell, Int>()
        var emptyMinValue = Int.MAX_VALUE - Constants.SIDE_COST * 2
        var emptyMinCell: Cell = matrixMap.matrix[0][0]

        queue.add(Pair(0, root))
        cost[root] = 0

        while (queue.isNotEmpty()) {
            val current = queue.removeHead()
            for (next in current.second.getNeighbors()) {
                if (Constants.SUPER_OBSTACLES.contains(next.cellType) || next.cellType == CellType.ROAD)
                    continue
                val costNext = cost[current.second]!! +
                        (if (Constants.OBSTACLES.contains(next.cellType))
                            Constants.SIDE_COST else Constants.EMPTY_COST)
                if (cost[next] == null || costNext < cost[next]!!) {
                    cost[next] = costNext
                    previous[next] = current.second

                    queue.add(Pair(costNext, next))

                    // cell in the next region that will be connected
                    if (Constants.EMPTY.contains(next.cellType) && costNext < emptyMinValue && costNext > 0) {
                        emptyMinValue = costNext
                        emptyMinCell = next
                    }
                }
            }
        }
        if (emptyMinCell == matrixMap.matrix[0][0])
            return false

        var routeCell = previous[emptyMinCell]
        while (routeCell != root) {
            if (Constants.OBSTACLES.contains(routeCell!!.cellType))
                routeCell.cellType = CellType.EMPTY
            routeCell = previous[routeCell]
        }
        return true
    }

    private fun calculateCostOfCell(queue: PriorityQueue<Pair<Int, Cell>>) {
        val current = queue.removeHead()

    }

    /**
     * used for finding squares 2x2 and 3x3
     */
    fun findAllNSquares(n: Int): List<Cell> {
        val res = mutableListOf<Cell>()

        for (x in 2..matrixMap.matrix.lastIndex)
            for (y in 2..matrixMap.matrix.lastIndex) {
                var isSquare = true
                for (x1 in -(n - 1)..0)
                    for (y1 in -(n - 1)..0)
                        if (!Constants.OBSTACLES.contains(matrixMap.matrix[x + x1][y + y1].cellType))
                            isSquare = false
                if (isSquare)
                    res.add(matrixMap.matrix[x][y])
            }

        return res
    }
}