package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.BinaryExpression
import wgslsmith.wgslgenerator.ast.expression.Expr
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.expression.IdentityLiteralExpression
import wgslsmith.wgslgenerator.ast.statement.Stat
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG.prob
import kotlin.random.Random
import kotlin.random.nextUInt

// PseudoRandomNumberGenerator
internal object PRNG {
    private var initialized = false
    var seed: Long = 0

    private lateinit var random: Random

    fun initializeWithSeed(seed: Long) {
        if (initialized) {
            throw Exception("Pseudorandom generator already initialized!")
        }
        this.seed = seed
        random = Random(seed)
        initialized = true
    }

    fun initializeWithoutSeed() {
        if (initialized) {
            throw Exception("Pseudorandom generator already initialized!")
        }
        initializeWithSeed(Random.Default.nextLong())
    }

    fun eval(probabilityThreshold: Double): Boolean {
        val generatedProbability = getRandomDoubleInRange(0.0, 1.0)
        if (generatedProbability > probabilityThreshold) {
            return false
        }
        return true
    }

    fun getRandomBool(): Boolean {
        return getRandomIntInRange(0, 2) != 0
    }

    private fun getRandomDoubleInRange(startDouble: Double, endDouble: Double): Double {
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

    fun getRandomTypeFrom(givenTypes: ArrayList<WGSLType>): WGSLType {
        var type: WGSLType? = null

        val totalProb = givenTypes.fold(0.0) { acc, givenType -> acc + prob(givenType) }
        val randomDouble = getRandomDoubleInRange(0.0, totalProb)
        var intervalProb = 0.0
        for (givenType in givenTypes) {
            intervalProb += prob(givenType)
            if (randomDouble <= intervalProb) {
                type = givenType
                break
            }
        }

        if (type == null) {
            throw Exception("Failure to select random type from $givenTypes!")
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
            if (type.nestedDepth + 1 < CNFG.maxArrayNestDepth) {
                possibleArrayElementTypes.add(
                    WGSLArrayType(
                        abstractWGSLScalarType, IdentityLiteralExpression(0), type.nestedDepth + 1
                    )
                )
            }

            val arrayElementType = if (type.elementType == abstractWGSLScalarType) {
                getRandomTypeFrom(possibleArrayElementTypes)
            } else {
                type.elementType
            }

            val arrayElementCount = if (type.elementCountValue == 0) {
                // will include const generation here once implemented
                val arrayElementCountValue = 1//getRandomIntInRange(1, CNFG.maxArrayElementCount)
                IdentityLiteralExpression(arrayElementCountValue)
            } else {
                type.elementCount
            }

            type = WGSLArrayType(arrayElementType, arrayElementCount, type.nestedDepth)
        }

        return type
    }

    fun getRandomTypeList(typeLists: ArrayList<ArrayList<WGSLType>>): ArrayList<WGSLType> {
        val typeListProbabilities = ArrayList(typeLists.map { typeList -> prob(typeList) })

        val totalProb = typeListProbabilities.fold(0.0) { acc, prob -> acc + prob }
        val randomDouble = getRandomDoubleInRange(0.0, totalProb)
        var intervalProb = 0.0
        for (i in 0 until typeListProbabilities.size) {
            intervalProb += typeListProbabilities[i]
            if (randomDouble <= intervalProb) {
                return typeLists[i]
            }
        }

        throw Exception("Failure to select random typeList from $typeLists!")
    }

    fun getRandomExprFrom(givenExprs: ArrayList<Expr>): Expr {
        var expr: Expr? = null

        val totalProb = givenExprs.fold(0.0) { acc, givenExpr -> acc + prob(givenExpr) }
        val randomDouble = getRandomDoubleInRange(0.0, totalProb)
        var intervalProb = 0.0
        for (givenExpr in givenExprs) {
            intervalProb += prob(givenExpr)
            if (randomDouble <= intervalProb) {
                expr = givenExpr
                break
            }
        }

        if (expr == null) {
            throw Exception("Failure to select random Expr from $givenExprs!")
        }

        return expr
    }

    fun getRandomStatFrom(stats: ArrayList<Stat>): Stat {
        val statIndex = getRandomIntInRange(0, stats.size)
        return stats[statIndex]
    }

    fun getNumberOfParentheses(): Int {
        var numberOfParentheses = 0

        while (eval(CNFG.generateParenthesesAroundExpression)
            && numberOfParentheses < CNFG.maxExcessExpressionParentheses) {
            numberOfParentheses++
        }

        return numberOfParentheses
    }

    fun getSubscriptInBound(symbolTable: SymbolTable, subscriptBound: Int) = getSubscriptInBoundAtDepth(
        symbolTable,
        subscriptBound, CNFG.maxExpressionNestDepth - CNFG.maxSubscriptAccessExpressionNestDepth
    )

    fun getSubscriptInBoundAtDepth(symbolTable: SymbolTable, subscriptBound: Int, depth: Int): String {
        val subscriptExpression = if (eval(CNFG.generateSimpleSubscriptAccess)) {
            val subscript = getRandomIntInRange(0, subscriptBound)
            IdentityLiteralExpression(subscript)
        } else {
            if (CNFG.ensureComplexSubscriptAccessInBounds) {
                val subscriptUnboundedExpression = ExpressionGenerator.getExpressionWithReturnType(
                    symbolTable, scalarIntType, depth + 1
                )
                BinaryExpression(
                    symbolTable, subscriptUnboundedExpression, IdentityLiteralExpression(subscriptBound)
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