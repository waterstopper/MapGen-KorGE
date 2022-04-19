package external

import com.soywiz.kmem.ByteArrayBuilder
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.children
import com.soywiz.korio.serialization.xml.readXml
import components.Surface
import steps.map.`object`.BuildingsManager
import util.Constants
import util.Constants.config
import util.Constants.mapEditorVersion
import util.Constants.matrixMap
import util.Constants.rnd
import util.Constants.zones
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeFormat
import com.soywiz.korio.file.std.rootLocalVfs
import components.CellType
import components.Fraction
import components.Resource
import steps.Guards
import steps.map.`object`.Castle
import steps.obstacle.ObstacleMapManager
import kotlin.math.pow

/**
 * Class for exporting to .hmm
 */
class Writer(
    private val obstacleMapManager: ObstacleMapManager,
    private val buildingsManager: BuildingsManager,
    private val guards: Guards
) {
    private var file = rootLocalVfs[config.exportPath]

    init {
        file = rootLocalVfs[config.exportPath]
    }

    private val byteArrayBuilder = ByteArrayBuilder()

    fun getPath() = file.path

    companion object {
        const val KEY = 1982026360
        const val VER = 25
        const val LNG_MASK = 1
        const val I_NODE_SIZE = 2
    }

    suspend fun writeHeader() {
        writeNBytes(KEY, 4) // header
        writeNBytes(VER, 4) // version
        writeSize()
        writeNBytes(LNG_MASK, 4) // map language mask
        writeNBytes(2, 4) // cnt

        writeString("Map Description")
        writeNBytes(2, 2) // tet
        writeString("Default description")
        // do not need tet here
        writeString("Map name")
        writeNBytes(1, 2) // tet
        writeString(Constants.template.name)
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
        writeResources()
        //writeNBytes(0, 2)
        // guards count
        writeGuards()
        // events count
        writeNBytes(0, 2)
        // visitables count
        writeVisitables()
        // ownerables count
        writeMines()
        // castles
        writeCastles()
        // map dump
        writeSurface()
        // write decorations
        DecorationsBuilder(obstacleMapManager).placeObstacles(this)

        // paths count
        writeRoads()
        changeFile()
        file.write(byteArrayBuilder.data)
    }

    private suspend fun changeFile() {
        file = rootLocalVfs[config.exportPath.substring(0, config.exportPath.length - 4)
                + DateTime.now().time.format(TimeFormat.FORMAT_TIME).replace(':', '_')
                + ".hmm"]
        if (file.exists()) {
            println("exists")
            file = rootLocalVfs[file.path.substring(0, file.path.length - 4) + "(1).hmm"]
        }

    }

    private fun writeGuards() {
        val guards = guards.guards.filter { it.level >= 0 }
        writeNBytes(guards.size, 2)
        for (guard in guards) {
            writeNBytes(3840 + guard.level, 2)
            writeNBytes(config.guardCount[guard.level].value, 4)
            // disp
            writeNBytes(1, 1)
            // notGrow
            writeNBytes(0, 1)
            writePoint(guard.position)
            if (mapEditorVersion > 0x18)
                writeString("")
        }
    }

    private fun writeSize() {
        writeNBytes(
            when (config.mapSize) {
                32 -> 0
                64 -> 1
                128 -> 2
                256 -> 3
                else -> throw Exception("unsupported export map size")
            }, 1
        ) // size
    }

    private fun writeRoads() {
        writeNBytes(matrixMap.matrix.count { it.cellType == CellType.ROAD }, 4)
        for (x in 0 until matrixMap.matrix.width)
            for (y in 0 until matrixMap.matrix.height) {
                if (matrixMap.matrix[x, y].cellType == CellType.ROAD) {
                    writePoint(x, y)
                    writeString(
                        when (matrixMap.matrix[x, y].zone.type) {
                            Surface.LAVA -> "stone_road"
                            Surface.SNOW -> "snow_road"
                            else -> "dirt_road"
                        }
                    )
                }
            }
    }

    private fun writeVisitables() {
        writeNBytes(zones.sumOf { it.teleports.size }, 2)
        for (z in zones) {
            for (teleport in z.teleports) {
                writeString(teleport.toString())
                writePoint(teleport.position)
                writeArmy()
                writeString("")
            }
        }
    }

    private fun writePlayers() {
        val numPlayers = zones.maxOf { z -> z.buildings.maxOf { it.playerColor } } + 1
        writeNBytes(numPlayers, 2)
        for (playerIndex in 1..numPlayers) {
            // id
            writeNBytes(playerIndex, 1)
            // player type mask
            var pCastle: Castle? = null
            zones.forEach { zone ->
                val castle = zone.castles.find { it.playerColor == playerIndex - 1 }
                if (castle != null) pCastle = castle
            }
            if (pCastle != null) {
                writeNBytes(pCastle!!.fraction.ordinal, 1)
                // has main castle
                writeNBytes(1, 1)

                writePoint(pCastle!!.position)
                // create hero in castle
                if (config.spawnHeroAtCastle.value)
                    writeNBytes(1, 1)
                else
                    writeNBytes(0, 1)
            } else {
                writeNBytes(Fraction.getRandom().ordinal, 1)
                writeNBytes(0, 1)
            }
        }
    }

    private suspend fun writeResources() {
        val artifacts = resourcesVfs["artifacts.xml"].readXml().children("Item").toMutableList()
        writeNBytes(guards.treasures.size, 2)
        for (treasure in guards.treasures) {
            /**
             * type 0 - res
             * 1 - mana crystal
             * 2 - campfire
             * 3 - chest
             * 4 - artifact
             */
            writeNBytes(treasure.type.ordinal, 1)
            writePoint(treasure.position)
            writeArmy()
            writeString("")
            when (treasure.type.ordinal) {
                0 -> {
                    writeNBytes(Resource.getRandomExcept(listOf()).ordinal, 1) // resource type
                    writeNBytes(0, 4) // amount, 0 for random
                }
                1 -> writeNBytes(0, 4) // amount
                4 -> {
                    writeString(artifacts.random(rnd).attributes["id"]!!) // artifact name
                }
                else -> {
                }
            }
        }
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
    }

    private fun writeSurface() {
        val size = matrixMap.matrix.width
        val len = ((size + 1).toDouble().pow(2) * I_NODE_SIZE).toInt()
        // transpose matrix - this way it resembles the view in "visualizeMatrix()" in KorGE
        for (y in 0 until size) {
            for (x in 0 until size)
                writeNBytes(matrixMap.matrix[x, y].zone.type.ordinal, 2)

            writeNBytes(matrixMap.matrix[matrixMap.matrix.width - 1, y].zone.type.ordinal, 2)
        }
        for (i in 0..matrixMap.matrix.width)
            writeNBytes(matrixMap.matrix[matrixMap.matrix.width - 1, matrixMap.matrix.width - 1].zone.type.ordinal, 2)

        //writeNBytes(0, len)
    }

    private fun writeString(str: String) {
        val arr = str.toCharArray()

        writeNBytes(arr.size, 4)
        for (i in 0..arr.lastIndex)
            writeNBytes(arr[i].code, 2)
    }

    class DecorationsBuilder(obstacleMapManager: ObstacleMapManager) {
        private val obstacleFolders = listOf("mountains", "trees", "decals")
        private lateinit var groups: MutableMap<Int, Map<Surface, List<Xml>>>
        private val obstacle3Squares = createNSquares(obstacleMapManager, 3)
        private val obstacle2Squares = createNSquares(obstacleMapManager, 2)

        private var amount3 =
            ((obstacle3Squares.lastIndex / 4)..(obstacle3Squares.lastIndex * 4 / 5)).random(rnd)
        private var amount2 =
            ((obstacle2Squares.lastIndex / 4)..(obstacle2Squares.lastIndex * 4 / 5)).random(rnd)
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
                Array(matrixMap.matrix.width)
                { BooleanArray(matrixMap.matrix.width) { false } }

            //writeUnknown(groups)
            for (i in parseGroups()) {
                if (i.value[0].children("PassModifier").count() != 0)
                    groups[i.key] = groupBySurface(i.value)
            }

            writer.writeNBytes(amount3 + amount2 + amount1, 4)

            for (i in obstacle3Squares.shuffled(rnd).take(amount3)) {
                val decoration = groups[9]!![i.first]?.random(rnd)
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

            for (i in obstacle2Squares.shuffled(rnd).take(amount2)) {
                val decoration = groups[4]!![i.first]?.random(rnd)
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
                    val cell = matrixMap.matrix[x, y]
                    if (Constants.OBSTACLES.contains(cell.cellType) && !placedMatrix[x][y]) {
                        writer.writeDecoration(
                            cell.position,
                            groups[1]!![cell.zone.type]!!.random(rnd).attribute("id")!!
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
            // name of surface - list of decorations
            val res = mutableMapOf<String, MutableList<Xml>>()
            // get combat decorations and if decoName == combatDecos[i].name add
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