package export

import com.soywiz.korio.file.std.resourcesVfs
import steps.Voronoi
import kotlin.math.pow

class Writer(val map: Voronoi) {
    val file = resourcesVfs["exported.hmm"]
    var ptr = 0

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
        writeNBytes(0, 2)
        writeNBytes(0, 2)
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
        writeNBytes(0, 4)

        // paths count
        writeNBytes(0, 4)
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
        for (list in map.matrixMap.matrix) {
            for (i in list) {
                writeNBytes(i.zone.type.ordinal, 2)
            }
        }

        //writeNBytes(0, len)
    }

    private suspend fun writeString(str: String) {
        val arr = str.toCharArray()

        writeNBytes(arr.size, 4)
        for (i in 0..arr.lastIndex)
            writeNBytes(arr[i].code, 2)
    }
}