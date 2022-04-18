package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal object ExpressionGenerator {
    fun getExpressionWithoutReturnType(symbolTable: SymbolTable, depth: Int): Expression {
        return getExpressionFromList(symbolTable, null, allExprs, depth)
    }

    // need to alter this for more complex types!
    fun getExpressionWithReturnType(symbolTable: SymbolTable, returnType: WGSLType, depth: Int): Expression {
        val exprs = ExprTypes.getExprs(returnType)
        return getExpressionFromList(symbolTable, returnType, exprs, depth)
    }

    private fun getExpressionFromList(
        symbolTable: SymbolTable, givenReturnType: WGSLType?, exprs: ArrayList<Expr>, depth: Int
    ): Expression {
        val possibleExprs = ArrayList<Expr>()
        if (depth >= CNFG.maxExpressionRecursion) {
            possibleExprs += IdentityUniversalExpr.values().asList()
            if (givenReturnType != null && givenReturnType is WGSLScalarType) {
                possibleExprs += IdentityLiteralExpr.values().asList()
            } else if (givenReturnType != null) {
                possibleExprs += IdentityConstructorExpr.values().asList()
            } else {
                possibleExprs += IdentityLiteralExpr.values().asList()
                possibleExprs += IdentityConstructorExpr.values().asList()
            }
        } else {
            possibleExprs += exprs
        }

        val expr = PRNG.getRandomExprFrom(possibleExprs)
        val exprType = ExprTypes.typeOf(expr)
        val returnType = givenReturnType ?: PRNG.getRandomTypeFrom(exprType.types)

        return when (expr) {
            is IdentityExpr   -> {
                IdentityExpression().generate(symbolTable, returnType, expr, depth)
            }
            is ComparisonExpr -> {
                ComparisonExpression().generate(symbolTable, returnType, expr, depth)
            }
            is UnaryExpr      -> {
                UnaryExpression().generate(symbolTable, returnType, expr, depth)
            }
            is BinaryExpr     -> {
                BinaryExpression().generate(symbolTable, returnType, expr, depth)
            }
            is BuiltinExpr    -> {
                BuiltinExpression().generate(symbolTable, returnType, expr, depth)
            }
            else              -> throw Exception("Attempt to generate Expression with uncategorized Expr $expr!")
        }
    }
}

internal abstract class Expression {
    abstract var returnType: WGSLType
    abstract var expr: Expr

    abstract fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): Expression
}