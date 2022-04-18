package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityExpression : Expression() {
    private var symbol: Symbol? = null
    private var parentheses = 0

    // subExpression holds more complex code for constructor literal/zero value generation
    private var subExpression: Expression? = null

    //private var literal: String? = null
    //private val components = ArrayList<Expr>()

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): IdentityExpression {
        this.returnType = returnType
        this.expr = expr

        if (expr == IdentityUniversalExpr.SYMBOL) {
            // indicates that no symbol of matching type yet exists
            // instead of continuing, return a new identityExpression (constructor, literal or zero value)
            if (!symbolTable.hasWriteableOf(returnType)) {
                val replacementExprs: ArrayList<Expr> = if (returnType is WGSLScalarType) {
                    arrayListOf(IdentityLiteralExpr.LITERAL)
                } else {
                    arrayListOf(IdentityConstructorExpr.CONSTRUCTOR)
                }
                replacementExprs.add(IdentityUniversalExpr.ZERO_VALUE)

                this.expr = PRNG.getRandomExprFrom(replacementExprs)
            } else {
                symbol = symbolTable.getRandomSymbol(returnType)
            }
        }

        when (this.expr) {
            IdentityUniversalExpr.SYMBOL        -> {}
            IdentityConstructorExpr.CONSTRUCTOR -> {
                this.subExpression = IdentityConstructorExpression().generate(symbolTable, returnType, this.expr, depth)
            }
            IdentityLiteralExpr.LITERAL         -> {
                this.subExpression = IdentityLiteralExpression().generate(symbolTable, returnType, this.expr, depth)
            }
            IdentityUniversalExpr.ZERO_VALUE    -> {
                this.subExpression = IdentityZeroValExpression().generate(symbolTable, returnType, this.expr, depth)
            }
            else                                -> throw Exception(
                "Attempt to generate IdentityExpression of unknown Expr $this.expr!"
            )
        }

        while (PRNG.evaluateProbability(CNFG.probabilityParenthesesAroundIdentity)
            && parentheses < CNFG.maxParentheses) {
            parentheses++
        }

        return this
    }

    override fun toString(): String {
        var identityString = if (subExpression != null) {
            "$subExpression"
        } else if (symbol != null) {
            "$symbol"
        } else {
            throw Exception(
                "Attempt to generate string representation of IdentityExpression without symbol or subExpression!"
            )
        }

        if (CNFG.useExcessParentheses) {
            for (i in 1..parentheses) {
                identityString = "($identityString)"
            }
        }

        return identityString
    }
}