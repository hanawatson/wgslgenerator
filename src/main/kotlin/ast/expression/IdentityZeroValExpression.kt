package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class IdentityZeroValExpression : Expression {
    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = 0

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): IdentityZeroValExpression {
        this.returnType = returnType
        this.expr = expr

        return this
    }

    override fun toString(): String {
        return "$returnType()"
    }
}