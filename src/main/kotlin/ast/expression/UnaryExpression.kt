package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class UnaryExpression : Expression() {
    private lateinit var arg: Expression
    private lateinit var argType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): UnaryExpression {
        this.returnType = returnType
        this.expr = expr

        argType = returnType
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)

        return this
    }

    override fun toString(): String {
        // handles special case of negative arg literal causing incorrect interpretation
        // (should literals just be,,, positive and then can be negative if - is generated???)
        val argString = if (CNFG.useNecessaryExpressionParentheses && ((arg !is IdentityExpression
                    && arg !is AccessExpression && arg !is BuiltinExpression) || "$arg"[0] == '-')) {
            "($arg)"
        } else if (CNFG.useUsefulExpressionParentheses && arg is AccessExpression) {
            "($arg)"
        } else {
            "$arg"
        }

        if (arg.expr == UnaryLogicalExpr.NOT && arg.returnType !is WGSLVectorType) {
            print("")
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