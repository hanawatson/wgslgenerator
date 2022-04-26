package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {

    // subExpression holds more complex code for non-symbol generation
    var subExpression: Expression? = null
    private var symbol: Symbol? = null

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        if (expr == IdentityUniversalExpr.SYMBOL) {
            // indicates that no symbol of matching type yet exists
            // instead of continuing, find a new IdentityExpr (constructor, literal or zero value)
            if (!symbolTable.hasWriteableOf(returnType)) {
                val replacementExprs: ArrayList<Expr> = when (returnType) {
                    is WGSLScalarType -> arrayListOf(
                        IdentityScalarExpr.LITERAL,
                        IdentityUniversalExpr.ZERO_VALUE
                    )
                    is WGSLVectorType,
                    is WGSLMatrixType,
                    is WGSLArrayType  -> arrayListOf(
                        IdentityCompositeExpr.CONSTRUCTOR,
                        IdentityUniversalExpr.ZERO_VALUE
                    )
                    else              -> {
                        throw Exception(
                            "Attempt to generate replacement IdentityExpr list for unknown type $returnType!"
                        )
                    }
                }

                this.expr = PRNG.getRandomExprFrom(replacementExprs)
            } else {
                symbol = symbolTable.getRandomSymbol(returnType)
            }
        }
        when (this.expr) {
            IdentityUniversalExpr.SYMBOL      -> {}
            IdentityCompositeExpr.CONSTRUCTOR -> subExpression =
                IdentityConstructorExpression(symbolTable, returnType, this.expr, depth)
            IdentityScalarExpr.LITERAL        -> subExpression =
                IdentityLiteralExpression(returnType, this.expr)
            IdentityUniversalExpr.ZERO_VALUE  -> subExpression =
                IdentityZeroValExpression(returnType, this.expr)
            else                              -> throw Exception(
                "Attempt to generate IdentityExpression of unknown Expr $this.expr!"
            )
        }
    }

    override fun toString(): String {
        var identityExpressionString = if (subExpression != null) {
            "$subExpression"
        } else if (symbol != null) {
            "$symbol"
        } else {
            throw Exception(
                "Attempt to generate string representation of IdentityExpression without symbol or subExpression!"
            )
        }

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                identityExpressionString = "($identityExpressionString)"
            }
        }

        return identityExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<*> {
            return when (expr) {
                IdentityCompositeExpr.CONSTRUCTOR ->
                    IdentityConstructorExpression.argsForExprType(expr, returnType, configOption)
                IdentityScalarExpr.LITERAL        -> IdentityLiteralExpression.argsForExprType(expr, returnType)
                IdentityUniversalExpr.ZERO_VALUE  -> IdentityZeroValExpression.argsForExprType(expr, returnType)
                else                              -> arrayListOf(returnType)
            }
        }
    }
}