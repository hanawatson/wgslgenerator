package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

internal class BinaryExpression : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var lhsType: WGSLType
    private lateinit var rhsType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): BinaryExpression {
        this.returnType = returnType
        this.expr = expr

        lhsType = returnType
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, lhsType, depth + 1)

        rhsType = returnType
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)

        return this
    }

    override fun toString(): String {
        val binaryExpressionString = "$lhs ${expr.operator} $rhs"

        if (CNFG.useExpressionParentheses) {
            return "($binaryExpressionString)"
        }
        return binaryExpressionString
    }
}