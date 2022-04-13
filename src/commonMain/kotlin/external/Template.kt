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
        private var minesNum = RandomRange(0, 2)
        val mines: MutableList<TemplateMine> = mutableListOf()

        private var castlesNum = RandomRange(0, 1)
        val castles: MutableList<TemplateCastle> = mutableListOf()

        var connections: List<TemplateConnection> = listOf()

        init {
            while (mines.size < minesNum.value) {
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
                for (i in 0 until castlesNum.value)
                    castles.add(TemplateCastle())
            }
        }

        override fun toString(): String = "$surface,$size,$index,$richness"
    }

    @Serializable
    class TemplateConnection(
        val first: Int,
        val second: Int,
        val type: ConnectionType = ConnectionType.REGULAR,
        // make connection guard unified among all connections
        var guardLevel: RandomRange = RandomRange(-1, -1)
    ) {
        init {
            if (guardLevel.value == -1)
                guardLevel = Constants.config.connectionGuardLevel
        }
    }

    @Serializable
    data class TemplateCastle(val fraction: Fraction = Fraction.RANDOM, val player: Int = -1)

    @Serializable
    class TemplateMine(var resource: Resource, val player: Int = -1) {
        // make mine guard unified among all mines with config
        var guardLevel: RandomRange = RandomRange(-1, -1)

        init {
            if (guardLevel.value == -1)
                guardLevel = Constants.config.mineGuardLevel
            // should not happen
            if (resource == Resource.RANDOM)
                resource = Resource.getRandomExcept(emptyList())
        }
    }
}
