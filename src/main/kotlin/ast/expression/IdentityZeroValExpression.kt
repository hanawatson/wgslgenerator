package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType

internal class IdentityZeroValExpression(override val returnType: WGSLType, override var expr: Expr) : Expression {
    override var numberOfParentheses = 0

    override fun toString(): String {
        return "$returnType()"
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            return arrayListOf(returnType)
        }
    }
}