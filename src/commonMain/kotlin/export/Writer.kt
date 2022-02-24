package export

import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.children
import com.soywiz.korio.serialization.xml.readXml
import components.Biome
import steps.ObstacleMapManager
import steps.Voronoi
import kotlin.math.pow

class Writer(private val map: Voronoi, private val obstacleMapManager: ObstacleMapManager) {
    private val file = resourcesVfs["exported.hmm"]
    private var ptr = 0

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

        // players count
        writeNBytes(0, 2)

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
        writeNBytes(0, 2)

        // castles count
        writeNBytes(0, 2)

        // map dump
        writeSurface()

        // decorations count
        //writeNBytes(2, 4)

        DecorationsBuilder(obstacleMapManager).placeObstacles(this)

        // paths count
        writeNBytes(0, 4)
    }

    private suspend fun writeDecoration(x: Int, y: Int, decoName: String) {
        writePoint(x, y)
        writeString(decoName)
    }

    private suspend fun writePoint(x: Int, y: Int) {
        writeNBytes(x, 2)
        writeNBytes(y, 2)
    }

    private suspend fun writeNBytes(data: Int, n: Int) {
        val buffer = ByteArray(n)
        for (i in 0 until n) buffer[i] = (data shr (i * 8)).toByte()

        file.writeChunk(buffer, ptr.toLong())
        ptr += n
    }

    private suspend fun writeSurface() {
        val size = map.matrixMap.matrix.size
        val len = ((size + 1).toDouble().pow(2) * I_NODE_SIZE).toInt()

        // transpose matrix - this way it resembles the view in "visualizeMatrix()" in KorGE
        for (y in 0 until size) {
            for (x in 0 until size) {
                writeNBytes(map.matrixMap.matrix[x][y].zone.type.ordinal, 2)
            }
            writeNBytes(Biome.WATER.ordinal, 2)
        }
        for (i in 0..map.matrixMap.matrix.size)
            writeNBytes(Biome.WATER.ordinal, 2)

        //writeNBytes(0, len)
    }

    private suspend fun writeString(str: String) {
        val arr = str.toCharArray()

        writeNBytes(arr.size, 4)
        for (i in 0..arr.lastIndex)
            writeNBytes(arr[i].code, 2)
    }

    class DecorationsBuilder(obstacleMapManager: ObstacleMapManager) {
        private val obstacleFolders = listOf("mountains", "trees", "decals")
        private lateinit var groups: Map<Int, List<Xml>>
        private val obstacle3Squares = create3Squares(obstacleMapManager)
        private var amount = (obstacle3Squares.lastIndex / 3..obstacle3Squares.lastIndex).random()

        private lateinit var map: Map<String, List<Xml>>

        private fun create3Squares(oMM: ObstacleMapManager) = oMM.findAllNSquares(3)

        private suspend fun parseGroups(): Map<Int, List<Xml>> {
            val children = resourcesVfs["hmm.xml"].readXml()["Avatars"].children("Item")

            val groups = children.filter {
                it.children("Sprite")
                    .all { i -> obstacleFolders.contains(i.attribute("id").toString().lowercase().split(".")[0]) }
            }.groupBy { it.children("PassModifier").count() }

            return groups
        }

        suspend fun placeObstacles(writer: Writer) {
            groups = parseGroups()

            //writeUnknown(groups)

            writer.writeNBytes(amount, 4)

            for (i in obstacle3Squares.shuffled().take(amount))
                writer.writeDecoration(
                    i.position.first,
                    i.position.second,
                    groups[9]!!.random().attribute("id")!!
                )
        }

        suspend fun placeAll(writer: Writer) {
            groups = parseGroups()
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
                    "<Group id=\"${g.key}\">" + map["UNKNOWN"]!!.toString().filter { it != ',' } + "</Group>")
            }
        }

        /**
         * Method to group list of decorations of same size to surfaces
         * @return Map<surfaceName:list of decorations>
         */
        private suspend fun groupBySurface(decorations: List<Xml>): Map<String, List<Xml>> {
            // name of surface - list of decos
            val res = mutableMapOf<String, MutableList<Xml>>()
            // get combat decos and if decoName == combatDecos[i].name add
            val combatObstacles = resourcesVfs["hmm.xml"].readXml()["CombatObstacles"].children("Item")

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
                    res["UNKNOWN"]?.add(item) ?: run {
                        res["UNKNOWN"] = mutableListOf(item)
                    }
                }

            }

            return res
        }
    }
}