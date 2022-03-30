package steps.passage

import Constants
import components.Cell
import components.CellType
import components.Zone
import steps.voronoi.Voronoi

object GridPassage{
    /**
     * add passages at the edge of connected zones
     * @return true if passages created successfully
     */
    fun createPassages(): Boolean {
        val resolvedConnections = mutableListOf<Pair<Zone, Zone>>()
        val (goodCandidates, badCandidates) = createCandidates()
        for (conn in goodCandidates.keys)
            if (!resolveOnePassage(conn, goodCandidates, resolvedConnections))
                return false

        for (conn in badCandidates.keys)
            if (!resolvedConnections.contains(conn))
                if (!resolveOnePassage(conn, badCandidates, resolvedConnections))
                    return false
        return true
    }

    private fun createCandidates():
            Pair<Map<Pair<Zone, Zone>, MutableList<Cell>>,
                    Map<Pair<Zone, Zone>, MutableList<Cell>>> {
        // lists of cells with only one adjacent zone
        val goodCandidates = mutableMapOf<Pair<Zone, Zone>, MutableList<Cell>>()
        // lists with many adjacent zones
        val badCandidates = mutableMapOf<Pair<Zone, Zone>, MutableList<Cell>>()
        Voronoi.matrixMap.matrix.forEach { cell ->
            if (cell.adjacentEdges.isNotEmpty()) {
                // a good candidate
                if (cell.adjacentEdges.all {
                        it.zone == cell.adjacentEdges[0].zone
                                && it.adjacentEdges.all { i -> i.zone == cell.zone }
                    })
                    addCandidate(cell, cell.adjacentEdges[0], goodCandidates)
                // bad candidate
                else
                    for (c in cell.adjacentEdges)
                        addCandidate(cell, c, badCandidates)
            }
        }
        return Pair(goodCandidates, badCandidates)
    }

    /**
     * add Pair(cell.zone, compared.zone) to candidates map in order of zone indexes
     */
    private fun addCandidate(cell: Cell, compared: Cell, candidates: MutableMap<Pair<Zone, Zone>, MutableList<Cell>>) {
        if (cell.zone.getNullableConnection(compared.zone) != null) {
            val first = if (cell.zone.index > compared.zone.index)
                compared.zone else cell.zone
            val second = if (first == cell.zone) compared.zone else cell.zone
            if (candidates[Pair(first, second)] == null)
                candidates[Pair(first, second)] = mutableListOf()
            candidates[Pair(first, second)]!!.add(cell)
        }
    }

    /**
     * add passage to resolvedConnections, and choose random cell from candidates
     * that will be a passage
     */
    private fun resolveOnePassage(
        pass: Pair<Zone, Zone>,
        candidates: Map<Pair<Zone, Zone>, MutableList<Cell>>,
        resolvedConnections: MutableList<Pair<Zone, Zone>>
    ): Boolean {
        resolvedConnections.add(pass)
        pass.first.getNullableConnection(pass.second)!!.resolved = true
        // Why it was here?

//        if (candidates[pass]!!.size > 2) {
//            candidates[pass]!!.removeAt(0)
//            candidates[pass]!!.removeAt(candidates[pass]!!.lastIndex)
//        }
        var chosenCell = candidates[pass]!![(0..candidates[pass]!!.lastIndex).random(Constants.rnd)]
        var iter = 0
        // make sure that passages are not near
        while (chosenCell.getAllNeighbors().any { it.cellType == CellType.ROAD } && iter < 50) {
            chosenCell = candidates[pass]!![(0..candidates[pass]!!.lastIndex).random(Constants.rnd)]
            iter++
        }
        // created two roads near, unsuccessful generation
        if (chosenCell.getAllNeighbors().any { it.cellType == CellType.ROAD })
            return false

        val neighborOfChosen = chosenCell.adjacentEdges[(0..chosenCell.adjacentEdges.lastIndex).random(Constants.rnd)]

        chosenCell.cellType = CellType.ROAD
        neighborOfChosen.cellType = CellType.ROAD


//        for (cell in chosenCell.getAllNeighbors()) {
//
//            if (Constants.OBSTACLES.contains(cell.cellType))
//                cell.cellType = CellType.EDGE
//        }
//        for (cell in neighborOfChosen.getAllNeighbors()) {
//
//            if (Constants.OBSTACLES.contains(cell.cellType))
//                cell.cellType = CellType.EDGE
//        }
        // connect
        chosenCell.getOpposite(neighborOfChosen).cellType = CellType.EMPTY
        neighborOfChosen.getOpposite(chosenCell).cellType = CellType.EMPTY

        return true
    }
}