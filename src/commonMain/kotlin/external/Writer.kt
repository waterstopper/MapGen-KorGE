package external

import com.soywiz.kmem.ByteArrayBuilder
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.children
import com.soywiz.korio.serialization.xml.readXml
import components.Surface
import steps.building.BuildingsManager
import Constants
import steps.obstacle.ObstacleMapManager
import steps.voronoi.Voronoi
import kotlin.math.pow

class Writer(
    private val map: Voronoi,
    private val obstacleMapManager: ObstacleMapManager,
    val buildingsManager: BuildingsManager
) {
    private val file = resourcesVfs["exported.hmm"]
    private var ptr = 0
    private val byteArrayBuilder = ByteArrayBuilder()

    companion object {
        const val KEY = 1982026360
        const val VER = 25
        const val LNG_MASK = 1
        const val I_NODE_SIZE = 2
    }

    suspend fun writeHeader() {
        writeNBytes(KEY, 4) // header
        writeNBytes(VER, 4) // version
        writeNBytes(1, 1) // size
        writeNBytes(LNG_MASK, 4) // map language mask
        writeNBytes(2, 4) // cnt

        writeString("Map Description")
        writeNBytes(2, 2) // tet

        writeString("Default desctiption")
        // do not need tet here

        writeString("Map name")
        writeNBytes(1, 2) // tet

        writeString("My map")

        // empty strings
        writeNBytes(0, 4) // empty map version in ver>=0x15 (19)
        writeNBytes(0, 4) // empty author name

        // no time events
        writeNBytes(0, 2)

        // ultimate artifact position (x,y), each 2 bytes
        writePoint(0, 0)
        writeNBytes(0, 4) // something else with artifact (m_radUltimateArt)

        // players
        writePlayers()

        // heroes count
        writeNBytes(0, 2)

        // map objects count
        writeNBytes(0, 2)

        // guards count
        writeNBytes(0, 2)

        // events count
        writeNBytes(0, 2)

        // visitables count
        writeNBytes(0, 2)

        // ownerables count
        writeMines()

        // castles
        writeCastles()

        // map dump
        writeSurface()

        // decorations count
        //writeNBytes(2, 4)

        DecorationsBuilder(obstacleMapManager).placeObstacles(this)

        // paths count
        writeNBytes(0, 4)

        file.write(byteArrayBuilder.data)
    }

    private fun writePlayers() {
//        writeNBytes(buildingsManager.players.size, 2)
//        for (player in buildingsManager.players) {
//            // id
//            writeNBytes(player.color, 1)
//            // player type mask
//            writeNBytes(player.fraction.ordinal, 1)
//            // has main castle
//            writeNBytes(1, 1)
//            if (true) {
//                writePoint(player.castle.position)
//                // create hero in castle
//                writeNBytes(0, 1)
//            }
//        }
    }

    private fun writeCastles() {
        writeNBytes(buildingsManager.castles.size, 2)
        for (castle in buildingsManager.castles) {
            // id
            writeString(castle.orientation)
            //type
            writeNBytes(castle.fraction.ordinal, 1)
            // owner
            writeNBytes(castle.playerColor, 1)
            // position
            writePoint(castle.position)
            // creatures
            writeArmy()
            // write text
            writeString("")
            //constructions count
            writeNBytes(0, 2)
        }
    }

    private fun writeMines() {
        writeNBytes(buildingsManager.mines.size, 2)
        for (mine in buildingsManager.mines) {
            writeString(mine.toString())
            writeNBytes(mine.playerColor, 1)
            writePoint(mine.position)
            writeArmy()
        }
    }

    private fun writeArmy() {
        for (i in 0..6) {
            writeNBytes(-1, 2)
            writeNBytes(0, 4)
        }
    }

    private fun writeDecoration(position: Pair<Int, Int>, decoName: String) =
        writeDecoration(position.first, position.second, decoName)

    private fun writeDecoration(x: Int, y: Int, decoName: String) {
        writePoint(x, y)
        writeString(decoName)
    }

    private fun writePoint(point: Pair<Int, Int>) = writePoint(point.first, point.second)

    private fun writePoint(x: Int, y: Int) {
        writeNBytes(x, 2)
        writeNBytes(y, 2)
    }

    private fun writeNBytes(data: Int, n: Int) {
        val buffer = ByteArray(n)
        for (i in 0 until n) buffer[i] = (data shr (i * 8)).toByte()

        byteArrayBuilder.append(buffer)
        //file.writeChunk(buffer, ptr.toLong())
        ptr += n
    }

    private fun writeSurface() {
        val size = map.matrixMap.matrix.width
        val len = ((size + 1).toDouble().pow(2) * I_NODE_SIZE).toInt()

        // transpose matrix - this way it resembles the view in "visualizeMatrix()" in KorGE
        for (y in 0 until size) {
            for (x in 0 until size)
                writeNBytes(map.matrixMap.matrix[x, y].zone.type.ordinal, 2)

            writeNBytes(Surface.WATER.ordinal, 2)
        }
        for (i in 0..map.matrixMap.matrix.width)
            writeNBytes(Surface.WATER.ordinal, 2)

        //writeNBytes(0, len)
    }

    private fun writeString(str: String) {
        val arr = str.toCharArray()

        writeNBytes(arr.size, 4)
        for (i in 0..arr.lastIndex)
            writeNBytes(arr[i].code, 2)
    }

    class DecorationsBuilder(private val obstacleMapManager: ObstacleMapManager) {
        private val obstacleFolders = listOf("mountains", "trees", "decals")
        private lateinit var groups: MutableMap<Int, Map<Surface, List<Xml>>>
        private val obstacle3Squares = createNSquares(obstacleMapManager, 3)
        private val obstacle2Squares = createNSquares(obstacleMapManager, 2)

        private var amount3 =
            ((obstacle3Squares.lastIndex / 4)..(obstacle3Squares.lastIndex * 4 / 5)).random(Constants.rnd)
        private var amount2 =
            ((obstacle2Squares.lastIndex / 4)..(obstacle2Squares.lastIndex * 4 / 5)).random(Constants.rnd)
        private val amount1 = getAmountOfObstacles(obstacleMapManager)

        private lateinit var map: Map<Surface, List<Xml>>

        private fun createNSquares(oMM: ObstacleMapManager, n: Int) = oMM.findAllNSquares(n)
        private fun getAmountOfObstacles(oMM: ObstacleMapManager) = oMM.calculateAllObstacleCells()

        private suspend fun parseGroups(): Map<Int, List<Xml>> {
            val children = resourcesVfs["hmm.xml"].readXml()["Avatars"].children("Item")

            val groups = children.filter {
                it.children("Sprite")
                    .all { i -> obstacleFolders.contains(i.attribute("id").toString().lowercase().split(".")[0]) }
            }.groupBy { it.children("PassModifier").count() }

            return groups
        }

        suspend fun placeObstacles(writer: Writer) {
            groups = mutableMapOf()
            val placedMatrix =
                Array(obstacleMapManager.matrixMap.matrix.width)
                { BooleanArray(obstacleMapManager.matrixMap.matrix.width) { false } }

            //writeUnknown(groups)
            for (i in parseGroups()) {
                if (i.value[0].children("PassModifier").count() != 0)
                    groups[i.key] = groupBySurface(i.value)
            }

            writer.writeNBytes(amount3 + amount2 + amount1, 4)

            for (i in obstacle3Squares.shuffled(Constants.rnd).take(amount3)) {
                val decoration = groups[9]!![i.first]?.random(Constants.rnd)
                if (decoration != null) {
                    writer.writeDecoration(
                        i.second.position,
                        decoration.attribute("id")!!
                    )
                    for (x in -2..0)
                        for (y in -2..0)
                            placedMatrix[x + i.second.position.first][y + i.second.position.second] = true
                } else
                    writer.writeDecoration(0, 0, "flowers_8")
            }

            for (i in obstacle2Squares.shuffled(Constants.rnd).take(amount2)) {
                val decoration = groups[4]!![i.first]?.random(Constants.rnd)
                if (decoration != null) {
                    writer.writeDecoration(
                        i.second.position,
                        decoration.attribute("id")!!
                    )
                    for (x in -1..0)
                        for (y in -1..0)
                            placedMatrix[x + i.second.position.first][y + i.second.position.second] = true
                } else
                    writer.writeDecoration(0, 0, "flowers_8")
            }

            var amountLeft = amount1
            for (x in placedMatrix.indices)
                for (y in placedMatrix.indices) {
                    val cell = obstacleMapManager.matrixMap.matrix[x, y]
                    if (Constants.OBSTACLES.contains(cell.cellType) && !placedMatrix[x][y]) {
                        writer.writeDecoration(
                            cell.position,
                            groups[1]!![cell.zone.type]!!.random(Constants.rnd).attribute("id")!!
                        )
                        amountLeft--
                    }
                }

            for (i in 0 until amountLeft)
                writer.writeDecoration(1, 0, "flowers_8")

        }

        suspend fun placeAll(writer: Writer) {
            val groups = parseGroups()
            val allObjects = groups.values.flatten()

            writer.writeNBytes(allObjects.size, 4)
            var x = 0
            var y = 0
            for (part in allObjects) {
                writer.writeDecoration(x, y, part.attribute("id")!!)
                x += 3
                if (x > 64) {
                    y += 3
                    x = 0
                }
            }
        }

        private suspend fun writeUnknown(groups: Map<Int, List<Xml>>) {
            for (g in groups) {
                map = groupBySurface(g.value)

                resourcesVfs[g.key.toString() + ".xml"].writeString(
                    "<Group id=\"${g.key}\">" + map[Surface.RANDOM]!!.toString().filter { it != ',' } + "</Group>")
            }
        }

        /**
         * Method to group list of decorations of same size to surfaces
         * @return Map<surfaceName:list of decorations>
         */
        private suspend fun groupBySurface(decorations: List<Xml>): Map<Surface, List<Xml>> {
            // name of surface - list of decos
            val res = mutableMapOf<String, MutableList<Xml>>()
            // get combat decos and if decoName == combatDecos[i].name add
            val combatObstacles = resourcesVfs["hmm.xml"].readXml()["CombatObstacles"].children("Item")
            val customItems =
                resourcesVfs["${decorations[0].children("PassModifier").count()}.xml"]
                    .readXml().children("Item")

            for (item in decorations) {
                val same = combatObstacles.find {
                    item.children("Sprite").all { i -> i.attribute("id") == it.attribute("sprite") }
                }

                if (same != null)
                    for (surf in same.children("Surf"))
                        res[surf.attribute("id")]?.add(item) ?: run {
                            res[surf.attribute(("id"))!!] = mutableListOf(item)
                        }
                else {
                    val custom = customItems.find { item.attribute("id") == it.attribute("id") }
                    if (custom != null)
                        res[custom.child("Surf")!!.attribute("id")]?.add(item) ?: run {
                            res[custom.child("Surf")!!.attribute("id")!!] = mutableListOf(item)
                        }
                    //else res["UNKNOWN"]?.add(item) ?: run {
                    //    res["UNKNOWN"] = mutableListOf(item)
                    //}
                }

            }

            return res.mapKeys { Surface.valueOf(it.key.split("_").last()) }
        }
    }
}