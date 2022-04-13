package wgslsmith.wgslgenerator.ast.expression

import internalProgRep.Type
import internalProgRep.WGSLScalarType
import internalProgRep.WGSLType
import wgslsmith.wgslgenerator.internalProgRep.BinaryBitExpr
import wgslsmith.wgslgenerator.internalProgRep.Expr
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager

internal class UnaryExpression : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var lhsType: WGSLType
    private lateinit var rhsType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): UnaryExpression {
        this.returnType = returnType
        this.expr = expr

        lhsType = returnType
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, lhsType, depth + 1)

        rhsType =
            if (expr == BinaryBitExpr.SHIFT_LEFT || expr == BinaryBitExpr.SHIFT_RIGHT) {
                WGSLScalarType(Type.UNINT)
            } else {
                returnType
            }
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)

        return this
    }

    override fun toString(): String {
        val binaryExpressionString = "$lhs ${expr.operator} $rhs"

        if (ConfigurationManager.useExpressionParentheses) {
            return "($binaryExpressionString)"
        }
        return binaryExpressionString
    }
}