package external

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class RandomBoolean(@Transient private val bool: Int = -1) {
    val value: Boolean = if (bool == 0)
        Constants.rnd.nextBoolean()
    else bool > 0

}