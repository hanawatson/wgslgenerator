package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLTypeEnum
import kotlin.random.Random
import kotlin.random.nextUInt

object PseudoNumberGenerator {
    private var initialized = false
    private val WGSLEnumTypes = ArrayList(WGSLTypeEnum.values().asList())

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

    fun evaluateProbability(probabilityThreshold: Double): Boolean {
        val generatedProbability = getRandomDoubleInRange(0.0, 1.0)
        if (generatedProbability > probabilityThreshold) {
            return false
        }
        return true
    }

    fun getRandomBool(): Boolean {
        return getRandomIntInRange(0, 2) != 0
    }

    fun getRandomDoubleInRange(startDouble: Double, endDouble: Double): Double {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        return random.nextDouble(startDouble, endDouble)
    }

    fun getRandomFloat(): Float {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        // Kotlin has no "float in range" function, so we use its safe Double->Float cast function
        // We also need to add overflow/underflow checks to make certain the casting is OK.
        var result = getRandomDoubleInRange(Float.MIN_VALUE.toDouble(), Float.MAX_VALUE.toDouble()).toFloat()
        if (result == Float.POSITIVE_INFINITY || result.isNaN()) {
            return 0f
        }
        if (getRandomBool()) {
            result = result.unaryMinus()
            if (result == Float.NEGATIVE_INFINITY || result.isNaN()) {
                return 0f
            }
        }
        return result
    }

    // startIndex inclusive, endIndex exclusive
    fun getRandomIntInRange(startIndex: Int, endIndex: Int): Int {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        return random.nextInt(startIndex, endIndex)
    }

    fun getRandomUnIntInRange(startIndex: UInt, endIndex: UInt): UInt {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        return random.nextUInt(startIndex, endIndex)
    }

    fun getRandomWGSLType(): WGSLType {
        // if get mat or vec etc., return result of separate randomWGSLMatrix etc. - then can generate size and the like
        val typeIndex = getRandomIntInRange(0, WGSLEnumTypes.size)
        val typeEnum = WGSLEnumTypes[typeIndex]
        // if mat/vec ...
        return WGSLScalarType(typeEnum)
    }
}