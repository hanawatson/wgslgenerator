package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class IdentityZeroValExpression : Expression() {
    private lateinit var zeroValString: String

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): IdentityZeroValExpression {
        this.returnType = returnType
        this.expr = expr

        zeroValString = "$returnType()"

        return this
    }

    override fun toString(): String {
        return zeroValString
    }
}