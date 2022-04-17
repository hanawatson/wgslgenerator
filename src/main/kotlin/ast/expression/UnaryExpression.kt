package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

internal class UnaryExpression : Expression() {
    private lateinit var arg: Expression
    private lateinit var argType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): UnaryExpression {
        this.returnType = returnType
        this.expr = expr

        argType = returnType
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)

        return this
    }

    override fun toString(): String {
        // handle the special case of an attempt to apply the unary minus operator "-" to a negative
        // literal value (i.e. one with a "-" prefix), resulting in incorrect interpretation
        // as the decrement operator "--"
        val argString = if ("$arg"[0] == '-') {
            "($arg)"
        } else {
            "$arg"
        }
        val unaryExpressionString = "${expr.operator}$argString"

        if (CNFG.useExpressionParentheses) {
            return "($unaryExpressionString)"
        }
        return unaryExpressionString
    }
}