package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class BinaryExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var lhs: Expression
    private var rhs: Expression
    private var lhsType: WGSLType = returnType
    private var rhsType: WGSLType = returnType

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        if (expr is BinaryArithmeticNumericExpr || expr is BinaryArithmeticMatrixNumericExpr) {
            var mixedType: WGSLType? = null

            if (expr == BinaryArithmeticMatrixNumericExpr.MULT) {
                if (returnType is WGSLVectorType &&
                    PRNG.evaluateProbability(CNFG.probabilityReplaceVectorMultOperandWithOther)) {
                    val randomMixedType = if (returnType.componentType == scalarFloatType) {
                        PRNG.getRandomTypeFrom(
                            arrayListOf(returnType.componentType, WGSLVectorType(returnType.componentType, 0))
                        )
                    } else {
                        returnType.componentType
                    }
                    if (randomMixedType is WGSLVectorType) {
                        if (PRNG.getRandomBool()) {
                            lhsType = randomMixedType
                            rhsType = WGSLMatrixType(
                                randomMixedType.componentType, returnType.length, randomMixedType.length
                            )
                        } else {
                            lhsType = WGSLMatrixType(
                                randomMixedType.componentType, randomMixedType.length, returnType.length
                            )
                            rhsType = randomMixedType
                        }
                    } else {
                        mixedType = randomMixedType
                    }
                } else if (returnType is WGSLMatrixType) {
                    if (PRNG.evaluateProbability(CNFG.probabilityReplaceMatrixMultOperandWithOther)) {
                        mixedType = returnType.componentType
                    } else {
                        val randomDimension = PRNG.getRandomIntInRange(2, 5)
                        lhsType = WGSLMatrixType(returnType.componentType, randomDimension, returnType.length)
                        rhsType = WGSLMatrixType(returnType.componentType, returnType.width, randomDimension)
                    }
                }
            } else if (returnType is WGSLVectorType &&
                PRNG.evaluateProbability(CNFG.probabilityReplaceVectorNonMultOperandWithScalar)) {
                mixedType = returnType.componentType
            }

            if (mixedType != null) {
                if (PRNG.getRandomBool()) {
                    lhsType = mixedType
                } else {
                    rhsType = mixedType
                }
            }
        }
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, lhsType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)
    }

    // generate "lhs modulo rhs" expression
    constructor(symbolTable: SymbolTable, lhs: Expression, rhs: Expression) :
            this(symbolTable, scalarIntType, BinaryArithmeticNumericExpr.MOD, CNFG.maxExpressionRecursion) {
        this.lhs = lhs
        this.rhs = rhs
    }

    fun getRHS(): Expression = rhs

    override fun toString(): String {
        val lhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(lhs)
        val rhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(rhs)

        var binaryExpressionString = "$lhsString ${expr.operator} $rhsString"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                binaryExpressionString = "($binaryExpressionString)"
            }
        }

        return binaryExpressionString
    }
}