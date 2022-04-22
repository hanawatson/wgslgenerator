package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.BinaryExpression
import wgslsmith.wgslgenerator.ast.expression.Expr
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.expression.IdentityLiteralExpression
import wgslsmith.wgslgenerator.ast.statement.Stat
import wgslsmith.wgslgenerator.tables.SymbolTable
import kotlin.random.Random
import kotlin.random.nextUInt

// PseudoRandomNumberGenerator
internal object PRNG {
    private var initialized = false

    private lateinit var random: Random

    /*fun initializeWithSeed(seed: Long) {
        if (initialized) {
            throw Exception("Pseudorandom generator already initialized!")
        }
        random = Random(seed)
        initialized = true
    }*/

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

    fun getRandomPositiveFloat(): Float {
        if (!initialized) {
            throw Exception("Pseudorandom generator must be initialized before use!")
        }
        // Kotlin has no "float in range" function, so we use its safe Double->Float cast function
        // we also need to add overflow/underflow checks to make certain the cast result is defined in WGSL
        val result = getRandomDoubleInRange(Float.MIN_VALUE.toDouble(), Float.MAX_VALUE.toDouble()).toFloat()
        if (result == Float.POSITIVE_INFINITY || result.isNaN()) {
            return 0f
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

        val arrayIndex = types.indexOf(abstractWGSLArrayType)
        if (arrayIndex != -1) {
            type = types[arrayIndex]
        }

        if (type is WGSLScalarType && type == abstractWGSLScalarType) {
            type = getRandomTypeFrom(scalarTypes)
        }

        if (type is WGSLVectorType) {
            val vectorComponentType = if (type.componentType == abstractWGSLScalarType) {
                getRandomTypeFrom(scalarTypes)
            } else {
                type.componentType
            }
            if (vectorComponentType !is WGSLScalarType) {
                throw Exception("Attempt to generate WGSLVectorType with unknown componentType $vectorComponentType!")
            }

            val vectorLength = if (type.length == 0) {
                getRandomIntInRange(2, 5)
            } else {
                type.length
            }

            type = WGSLVectorType(vectorComponentType, vectorLength)
        }

        if (type is WGSLMatrixType) {
            val matrixComponentType = if (type.componentType == abstractWGSLScalarType) {
                getRandomTypeFrom(matrixComponentTypes)
            } else {
                type.componentType
            }
            if (matrixComponentType !is WGSLScalarType) {
                throw Exception("Attempt to generate WGSLMatrixType with unknown componentType $matrixComponentType!")
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

            type = WGSLMatrixType(matrixComponentType, matrixWidth, matrixLength)
        }

        if (type is WGSLArrayType) {
            val possibleArrayElementTypes = arrayElementTypes
            possibleArrayElementTypes.remove(abstractWGSLArrayType)

            // create an object similar to abstractWGSLArrayType but, importantly, one that tracks
            // the current recursive depth of the array being generated - this is to prevent infinite
            // recursion of array elements being arrays of arrays etc. themselves
            val nestedArrayType = WGSLArrayType(
                abstractWGSLScalarType,
                IdentityLiteralExpression().generateIntLiteral(0),
                type.nestedDepth + 1
            )
            if (nestedArrayType.nestedDepth < CNFG.maxArrayRecursion) {
                possibleArrayElementTypes.add(nestedArrayType)
            }

            val arrayElementType = if (type.elementType == abstractWGSLScalarType) {
                getRandomTypeFrom(possibleArrayElementTypes)
            } else {
                type.elementType
            }

            val arrayElementCount = if (type.elementCountValue == 0) {
                // will include const generation here once implemented
                val arrayElementCountValue = getRandomIntInRange(1, CNFG.maxArrayElementCount)
                IdentityLiteralExpression().generateIntLiteral(arrayElementCountValue)
            } else {
                type.elementCount
            }

            type = WGSLArrayType(arrayElementType, arrayElementCount, type.nestedDepth)
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

    fun getSubscriptInBound(symbolTable: SymbolTable, subscriptBound: Int) = getSubscriptInBoundAtDepth(
        symbolTable,
        subscriptBound, CNFG.maxExpressionRecursion - CNFG.maxSubscriptDepth
    )

    fun getSubscriptInBoundAtDepth(symbolTable: SymbolTable, subscriptBound: Int, depth: Int): String {
        val subscriptExpression = if (evaluateProbability(CNFG.probabilityGenerateSubscriptAccessInBounds)) {
            val subscript = getRandomIntInRange(0, subscriptBound)
            IdentityLiteralExpression().generateIntLiteral(subscript)
        } else {
            if (CNFG.ensureSubscriptAccessInBounds) {
                val subscriptUnboundedExpression = ExpressionGenerator.getExpressionWithReturnType(
                    symbolTable, scalarIntType, depth + 1
                )
                BinaryExpression().generateModWithIntExpressions(
                    symbolTable,
                    subscriptUnboundedExpression,
                    IdentityLiteralExpression().generateIntLiteral(subscriptBound)
                )
            } else {
                ExpressionGenerator.getExpressionWithReturnType(symbolTable, scalarIntType, depth)
            }
        }
        return "$subscriptExpression"
    }

    fun getConvenienceLetterInBound(subscriptBound: Int, useRGBA: Boolean): String {
        val convenienceLettering = if (useRGBA) "rgba" else "xyzw"
        val convenienceIndex = getRandomIntInRange(0, subscriptBound)
        return "${convenienceLettering[convenienceIndex]}"
    }
}