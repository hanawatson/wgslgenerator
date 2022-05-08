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
        val (lhsTypes, rhsTypes) = argsForExprType(expr, returnType, probEval(returnType)).unzip()
        lhsType = PRNG.getRandomTypeFrom(lhsTypes as ArrayList<WGSLType>)
        rhsType = rhsTypes[lhsTypes.indexOf(lhsType)]
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, lhsType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)
    }

    // generate "lhs modulo rhs" expression
    constructor(symbolTable: SymbolTable, lhs: Expression, rhs: Expression) :
            this(symbolTable, scalarIntType, BinaryArithmeticNumericExpr.MOD, CNFG.maxExpressionNestDepth) {
        this.lhs = lhs
        this.rhs = rhs
    }

    override fun toString(): String {
        val lhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(lhs)
        val rhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(rhs)

        var binaryExpressionString = "$lhsString ${expr.operator} $rhsString"

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                binaryExpressionString = "($binaryExpressionString)"
            }
        }

        return binaryExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<Pair<WGSLType, WGSLType>> {
            val argTypes = ArrayList<Pair<WGSLType, WGSLType>>()
            var specialCaseSelected = false

            if (expr is BinaryArithmeticNumericExpr || expr is BinaryArithmeticMatrixNumericExpr) {
                if (expr == BinaryArithmeticMatrixNumericExpr.MULT) {
                    if (returnType is WGSLVectorType) {
                        // replace vector mult operand with other
                        if (configOption) {
                            argTypes.add(Pair(returnType.componentType, returnType))
                            argTypes.add(Pair(returnType, returnType.componentType))
                            if (returnType.componentType == scalarFloatType) {
                                for (dimension in 2..4) {
                                    argTypes.add(
                                        Pair(
                                            WGSLVectorType(returnType.componentType, dimension),
                                            WGSLMatrixType(returnType.componentType, returnType.length, dimension)
                                        )
                                    )
                                    argTypes.add(
                                        Pair(
                                            WGSLMatrixType(returnType.componentType, dimension, returnType.length),
                                            WGSLVectorType(returnType.componentType, dimension)
                                        )
                                    )
                                }
                            }
                            specialCaseSelected = true
                        }
                    } else if (returnType is WGSLMatrixType) {
                        // replace matrix mult operand with other
                        if (configOption) {
                            argTypes.add(Pair(returnType.componentType, returnType))
                            argTypes.add(Pair(returnType, returnType.componentType))
                        } else {
                            for (dimension in 2..4) {
                                argTypes.add(
                                    Pair(
                                        WGSLMatrixType(returnType.componentType, dimension, returnType.length),
                                        WGSLMatrixType(returnType.componentType, returnType.width, dimension)
                                    )
                                )
                            }
                        }
                        specialCaseSelected = true
                    }
                } else if (returnType is WGSLVectorType) {
                    // replace vector non-mult operand with scalar
                    if (configOption) {
                        argTypes.add(Pair(returnType.componentType, returnType))
                        argTypes.add(Pair(returnType, returnType.componentType))
                        specialCaseSelected = true
                    }
                }
            }

            if (!specialCaseSelected) {
                argTypes.add(Pair(returnType, returnType))
            }

            return argTypes
        }

        fun probEval(returnType: WGSLType): Boolean {
            return when (returnType) {
                is WGSLMatrixType -> PRNG.eval(CNFG.replaceMatrixBinaryOperandWithOtherType)
                is WGSLVectorType -> PRNG.eval(CNFG.replaceVectorBinaryOperandWithOtherType)
                else              -> false
            }
        }
    }
}