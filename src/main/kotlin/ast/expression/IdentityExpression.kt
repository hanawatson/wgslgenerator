package wgslsmith.wgslgenerator.ast.expression

import internalProgRep.WGSLType
import wgslsmith.wgslgenerator.ast.Literal
import wgslsmith.wgslgenerator.ast.LiteralGenerator
import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class IdentityExpression : Expression() {
    private var symbol: Symbol? = null
    private var literal: Literal? = null
    private var parentheses = 0
    override lateinit var returnType: WGSLType

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int): IdentityExpression {
        this.returnType = expressionType
        symbol = symbolTable.getRandomSymbol(expressionType)
        if (symbol!!.getName() == "") {
            symbol = null
            literal = LiteralGenerator.getLiteral(expressionType)
        }

        while (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityParenthesesAroundIdentity)
            && parentheses < ConfigurationManager.maxParentheses) {
            parentheses++
        }

        return this
    }

    fun getLiteralAsIdentity(literal: Literal): IdentityExpression {
        this.returnType = literal.getType()
        this.literal = literal

        return this
    }

    override fun toString(): String {
        var identity = if (symbol != null) "$symbol" else "$literal"

        if (ConfigurationManager.useExcessParentheses) {
            for (i in 1..parentheses) {
                identity = "($identity)"
            }
        }

        return identity
    }
}