package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expr
import wgslsmith.wgslgenerator.ast.statement.Stat
import kotlin.random.Random
import kotlin.random.nextUInt

// PseudoRandomNumberGenerator
internal object PRNG {
    private var initialized = false

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
        // we also need to add overflow/underflow checks to make certain the cast result is defined in WGSL
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

    fun getRandomTypeFrom(types: ArrayList<WGSLType>): WGSLType {
        val typeIndex = getRandomIntInRange(0, types.size)
        var type = types[typeIndex]

        if (type is WGSLScalarType && type == abstractWGSLScalarType) {
            type = getRandomTypeFrom(scalarTypes)
        }

        if (type is WGSLVectorType) {
            val vectorType = if (type.componentType == abstractWGSLScalarType) {
                getRandomTypeFrom(scalarTypes)
            } else {
                type.componentType
            }
            if (vectorType !is WGSLScalarType) {
                throw Exception("Attempt to generate WGSLVectorType with unknown componentType $vectorType!")
            }

            val vectorLength = if (type.length == 0) {
                getRandomIntInRange(2, 5)
            } else {
                type.length
            }

            type = WGSLVectorType(vectorType, vectorLength)
        }

        if (type is WGSLMatrixType) {
            val matrixType = if (type.componentType == abstractWGSLScalarType) {
                getRandomTypeFrom(matrixComponentTypes)
            } else {
                type.componentType
            }
            if (matrixType !is WGSLScalarType) {
                throw Exception("Attempt to generate WGSLMatrixType with unknown componentType $matrixType!")
            }

            val matrixWidth = if (type.width == 0) {
                getRandomIntInRange(2, 5)
            } else {
                type.width
            }

            val matrixLength = if (type.length == 0) {
                getRandomIntInRange(2, 5)
            } else {
                type.length
            }

            type = WGSLMatrixType(matrixType, matrixWidth, matrixLength)
        }

        return type
    }

    fun getRandomExprFrom(exprs: ArrayList<Expr>): Expr {
        val exprIndex = getRandomIntInRange(0, exprs.size)
        return exprs[exprIndex]
    }

    fun getRandomStatFrom(stats: ArrayList<Stat>): Stat {
        val statIndex = getRandomIntInRange(0, stats.size)
        return stats[statIndex]
    }

    fun getNumberOfParentheses(): Int {
        var numberOfParentheses = 0

        while (evaluateProbability(CNFG.probabilityParenthesesAroundExpression)
            && numberOfParentheses < CNFG.maxParentheses) {
            numberOfParentheses++
        }

        return numberOfParentheses
    }
}