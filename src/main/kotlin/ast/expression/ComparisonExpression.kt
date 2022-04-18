package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class ComparisonExpression : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var argType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): ComparisonExpression {
        this.returnType = returnType
        this.expr = expr

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

        return this
    }

    override fun toString(): String {
        val comparisonExpressionString = "$lhs ${expr.operator} $rhs"

        if (CNFG.useExpressionParentheses) {
            return "($comparisonExpressionString)"
        }
        return comparisonExpressionString
    }
}