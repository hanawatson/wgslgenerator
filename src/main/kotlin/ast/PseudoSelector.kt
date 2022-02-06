package wgslsmith.wgslgenerator.ast

import kotlin.random.Random

object PseudoSelector {
    private var initialized: Boolean = false
    private lateinit var random: Random

    fun initializeWithSeed(seed: Long) {
        if (initialized) {
            throw Exception("Pseudorandom generator already initialized!")
        }
        random = Random(seed)
        initialized = true
    }

    fun initializeWithoutSeed() {
        if (initialized) {
            throw Exception("Pseudorandom generator already initialized!")
        }
        random = Random.Default
        initialized = true
    }

    // startIndex inclusive, endIndex exclusive
    fun getRandomInRange(startIndex: Int, endIndex: Int): Int {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        return random.nextInt(startIndex, endIndex)
    }
}