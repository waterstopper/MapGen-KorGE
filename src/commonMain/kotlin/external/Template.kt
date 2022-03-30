package external

import components.ConnectionType
import components.Fraction
import components.Resource
import components.Surface
import kotlinx.serialization.Serializable
import Constants
import kotlin.properties.Delegates
import kotlin.random.nextInt

@Serializable
class Template(val name: String, val zones: List<TemplateZone>, val connections: List<TemplateConnection>) {

    @Serializable
    class TemplateZone(val surface: Surface, val size: Int, val index: Int, var richness: Int = 14) {
        private var minesMin = -1
        private var minesMax = -1
        val mines: MutableList<TemplateMine> = mutableListOf()

        private var castlesMin = -1
        private var castlesMax = -1
        val castles: MutableList<TemplateCastle> = mutableListOf()
        var connections: List<TemplateConnection> = listOf()

        init {
            val minesNum = Constants.rnd.nextInt(minesMin..minesMax + 1)
            while (mines.size < minesNum) {
                if (Constants.config.prioritizeBaseMines.value && !Resource.baseResources()
                        .all { res -> mines.any { it.resource == res } }
                )
                    mines.add(TemplateMine(Resource.getRandomBaseExcept(mines.map { it.resource })))
                else if (mines.size >= Resource.values().size)
                    mines.add(TemplateMine(Resource.getRandomExcept(emptyList())))
                else
                    mines.add(TemplateMine(Resource.getRandomExcept(mines.map { it.resource })))
            }

            if (castles.isEmpty()) {
                val castlesNum = Constants.rnd.nextInt(castlesMin..castlesMax + 1)
                for (i in 0 until castlesNum)
                    castles.add(TemplateCastle())
            }
        }
    }

    @Serializable
    class TemplateConnection(
        val first: Int,
        val second: Int,
        val type: ConnectionType = ConnectionType.REGULAR,
        private val guardLevelMin: Int = 0,
        private val guardLevelMax: Int = 1,
    ) {
        var guardLevel: Int = -1

        init {
            if (guardLevel == -1)
                guardLevel = Constants.rnd.nextInt(guardLevelMin..guardLevelMax + 1)
        }
    }

    @Serializable
    data class TemplateCastle(val fraction: Fraction = Fraction.RANDOM, val player: Int = -1)

    @Serializable
    class TemplateMine(var resource: Resource, val player: Int = -1) {
        private val guardLevelMin: Int = 0
        private val guardLevelMax: Int = 1
        var guardLevel = -1

        init {
            if (guardLevel == -1)
                guardLevel = Constants.rnd.nextInt(guardLevelMin..guardLevelMax + 1)
            // should not happen
            if (resource == Resource.RANDOM)
                resource = Resource.getRandomExcept(emptyList())

        }
    }
}
