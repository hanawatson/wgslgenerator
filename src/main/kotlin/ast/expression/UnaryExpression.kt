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
        val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType))
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
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

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                unaryExpressionString = "($unaryExpressionString)"
            }
        }

        return unaryExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            return arrayListOf(returnType)
        }
    }
}