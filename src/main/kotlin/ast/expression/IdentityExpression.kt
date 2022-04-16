package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Literal
import wgslsmith.wgslgenerator.ast.LiteralGenerator
import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class IdentityExpression : Expression() {
    private var symbol: Symbol? = null
    private var literal: Literal? = null
    private var parentheses = 0

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): IdentityExpression {
        this.returnType = returnType
        this.expr = expr

        if (expr == IdentityExpr.ID) {
            symbol = symbolTable.getRandomSymbol(returnType)
            // indicates that a literal value, not an existing symbol, should be generated
            if (symbol!!.name == "") {
                symbol = null
                literal = LiteralGenerator.getLiteral(returnType)
            }
        } else if (expr == IdentityExpr.ZERO_VALUE) {
            symbol = Symbol("${returnType.type.wgslType}()", returnType)
        }

        while (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityParenthesesAroundIdentity)
            && parentheses < ConfigurationManager.maxParentheses) {
            parentheses++
        }

        return this
    }

    fun getLiteralAsIdentity(literal: Literal): IdentityExpression {
        this.returnType = literal.getType()
        this.expr = IdentityExpr.ID
        this.literal = literal

        return this
    }

    override fun toString(): String {
        var identityString = if (symbol != null) "$symbol" else "$literal"

        if (ConfigurationManager.useExcessParentheses) {
            for (i in 1..parentheses) {
                identityString = "($identityString)"
            }
        }

        return identityString
    }
}