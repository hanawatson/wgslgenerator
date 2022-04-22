package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class UnaryExpression : Expression {
    private lateinit var arg: Expression

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): UnaryExpression {
        this.returnType = returnType
        this.expr = expr

        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, returnType, depth + 1)

        return this
    }

    override fun toString(): String {
        val argString = if (CNFG.useNecessaryExpressionParentheses && (arg !is IdentityExpression
                    && arg !is AccessExpression && arg !is BuiltinExpression)) {
            "($arg)"
        } else if (CNFG.useUsefulExpressionParentheses && arg is AccessExpression) {
            "($arg)"
        } else {
            "$arg"
        }

        var unaryExpressionString = "${expr.operator}$argString"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                unaryExpressionString = "($unaryExpressionString)"
            }
        }

        return unaryExpressionString
    }
}