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
    private var argType: WGSLType

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        val comparableTypes: ArrayList<WGSLType> = arrayListOf(scalarFloatType, scalarIntType, scalarUnIntType)
        if (expr is ComparisonEqExpr) {
            comparableTypes.add(scalarBoolType)
        }
        val argInnerType = PRNG.getRandomTypeFrom(comparableTypes) as WGSLScalarType
        argType = when {
            returnType.isRepresentedBy(vectorBoolType) -> WGSLVectorType(
                argInnerType, (returnType as WGSLVectorType).length
            )
            returnType.isRepresentedBy(scalarBoolType) -> argInnerType
            else                                       -> throw Exception(
                "Attempt to generate ComparisonExpression of unknown returnType $returnType!"
            )
        }
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
    }

    override fun toString(): String {
        val lhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(lhs)
        val rhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(rhs)

        var comparisonExpressionString = "$lhsString ${expr.operator} $rhsString"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                comparisonExpressionString = "($comparisonExpressionString)"
            }
        }

        return comparisonExpressionString
    }
}