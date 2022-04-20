package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLMatrixType
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
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

    fun getExpressionWithoutReturnType(symbolTable: SymbolTable, depth: Int): Expression {
        return getExpressionFromList(symbolTable, null, allExprs, depth)
    }

    fun getExpressionWithReturnType(symbolTable: SymbolTable, returnType: WGSLType, depth: Int): Expression {
        val exprs = ExprTypes.getExprs(returnType)
        return getExpressionFromList(symbolTable, returnType, exprs, depth)
    }

    private fun getExpressionFromList(
        symbolTable: SymbolTable, givenReturnType: WGSLType?, exprs: ArrayList<Expr>, depth: Int
    ): Expression {
        val possibleExprs = ArrayList<Expr>()
        if (depth >= CNFG.maxExpressionRecursion && givenReturnType != null) {
            possibleExprs += IdentityUniversalExpr.values().asList()
            possibleExprs += when (givenReturnType) {
                is WGSLScalarType -> IdentityScalarExpr.values().asList()
                is WGSLVectorType -> IdentityCompositeExpr.values().asList()
                is WGSLMatrixType -> IdentityCompositeExpr.values().asList()
                else              -> throw Exception(
                    "Unable to generate non-recursive Expression for unknown type $givenReturnType!"
                )
            }
        } else {
            possibleExprs += exprs
        }

        val expr = PRNG.getRandomExprFrom(possibleExprs)
        val exprType = ExprTypes.typeOf(expr)
        val returnType = givenReturnType ?: PRNG.getRandomTypeFrom(exprType.types)

        return when (expr) {
            is AccessExpr     -> AccessExpression()
            is BinaryExpr     -> BinaryExpression()
            is ConversionExpr -> ConversionExpression()
            is BuiltinExpr    -> BuiltinExpression()
            is ComparisonExpr -> ComparisonExpression()
            is DataExpr       -> DataExpression()
            is IdentityExpr   -> IdentityExpression()
            is UnaryExpr      -> UnaryExpression()
            else              -> throw Exception("Attempt to generate Expression with uncategorized Expr $expr!")
        }.generate(symbolTable, returnType, expr, depth)
    }
}

internal abstract class Expression {
    abstract var returnType: WGSLType
    abstract var expr: Expr
    abstract var numberOfParentheses: Int

    abstract fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): Expression
}