package util

import util.Constants.rnd
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Random that after evaluation will always return same integer
 * @param min minimal integer (inclusive)
 * @param max maximal integer (inclusive)
 */
@Serializable
class RandomRange(private val min: Int, private val max: Int) {
    @Transient
    val value = rnd.nextInt(min, max + 1)
}