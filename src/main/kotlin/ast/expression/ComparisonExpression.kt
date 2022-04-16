package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

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

        lhs = ExpressionGenerator.getExpressionWithNumericReturnType(symbolTable, depth + 1)
        argType = lhs.returnType
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