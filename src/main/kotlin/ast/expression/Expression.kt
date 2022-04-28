package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal object ExpressionGenerator {
    fun getUsefulParenthesizedExpressionString(expression: Expression): String {
        return if (CNFG.useUsefulExpressionParentheses && (expression is BinaryExpression
                    || expression is ComparisonExpression)) {
            "($expression)"
        } else {
            "$expression"
        }
    }

    fun getExpressionWithoutReturnType(symbolTable: SymbolTable, depth: Int) =
        getExpressionFromList(symbolTable, PRNG.getRandomTypeFrom(allTypes), null, depth)

    fun getExpressionWithReturnType(symbolTable: SymbolTable, returnType: WGSLType, depth: Int) =
        getExpressionFromList(symbolTable, returnType, ExprTypes.getExprs(returnType), depth)

    private fun getExpressionFromList(
        symbolTable: SymbolTable, returnType: WGSLType, exprs: ArrayList<Expr>?, depth: Int
    ): Expression {
        val possibleExprs = ArrayList<Expr>()
        if (depth >= CNFG.maxExpressionNestDepth - 1) {
            possibleExprs += IdentityUniversalExpr.values().asList()
            possibleExprs += when (returnType) {
                is WGSLScalarType -> IdentityScalarExpr.values().asList()
                is WGSLVectorType,
                is WGSLMatrixType,
                is WGSLArrayType  -> IdentityCompositeExpr.values().asList()
                else              -> throw Exception(
                    "Unable to generate non-recursive Expression for unknown type $returnType!"
                )
            }
        } else if (exprs != null) {
            possibleExprs += exprs
        } else {
            possibleExprs += ExprTypes.getExprs(returnType)
        }

        return when (val expr = PRNG.getRandomExprFrom(possibleExprs)) {
            is AccessExpr     -> AccessExpression(symbolTable, returnType, expr, depth)
            is BinaryExpr     -> BinaryExpression(symbolTable, returnType, expr, depth)
            is ConversionExpr -> ConversionExpression(symbolTable, returnType, expr, depth)
            is BuiltinExpr    -> BuiltinExpression(symbolTable, returnType, expr, depth)
            is ComparisonExpr -> ComparisonExpression(symbolTable, returnType, expr, depth)
            is DataExpr       -> DataExpression(symbolTable, returnType, expr, depth)
            is IdentityExpr   -> IdentityExpression(symbolTable, returnType, expr, depth)
            is UnaryExpr      -> UnaryExpression(symbolTable, returnType, expr, depth)
            else              -> throw Exception("Attempt to generate Expression with uncategorized Expr $expr!")
        }
    }
}

internal interface Expression {
    val returnType: WGSLType
    var expr: Expr
    var numberOfParentheses: Int
}

internal interface ExpressionCompanion {
    fun argsForExprType(expr: Expr, returnType: WGSLType, configOption: Boolean = false): ArrayList<*>
}