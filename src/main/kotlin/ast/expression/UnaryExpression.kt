package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class UnaryExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var arg: Expression

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, returnType, depth + 1)
    }

    override fun toString(): String {
        val argString = if (CNFG.useNecessaryExpressionParentheses && (arg is BinaryExpression
                    || arg is ComparisonExpression || arg is UnaryExpression)) {
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