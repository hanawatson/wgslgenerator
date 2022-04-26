package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class ComparisonExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var lhs: Expression
    private var rhs: Expression

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType))
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
    }

    override fun toString(): String {
        val lhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(lhs)
        val rhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(rhs)

        var comparisonExpressionString = "$lhsString ${expr.operator} $rhsString"

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                comparisonExpressionString = "($comparisonExpressionString)"
            }
        }

        return comparisonExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            val comparableTypes: ArrayList<WGSLType> = arrayListOf(scalarFloatType, scalarIntType, scalarUnIntType)
            if (expr is ComparisonEqExpr) {
                comparableTypes.add(scalarBoolType)
            }

            val argTypes = ArrayList<WGSLType>()
            for (comparableType in comparableTypes) {
                argTypes.add(
                    when {
                        returnType.isRepresentedBy(vectorBoolType) -> WGSLVectorType(
                            comparableType as WGSLScalarType, (returnType as WGSLVectorType).length
                        )
                        returnType.isRepresentedBy(scalarBoolType) -> comparableType
                        else                                       -> throw Exception(
                            "Attempt to generate ComparisonExpression argTypes of unknown returnType $returnType!"
                        )
                    }
                )
            }

            return argTypes
        }
    }
}