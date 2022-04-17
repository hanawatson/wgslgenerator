package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Literal
import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal object ExpressionGenerator {
    fun getLiteralAsExpression(literal: Literal): Expression {
        return IdentityExpression().getLiteralAsIdentity(literal)
    }

    fun getExpressionWithoutReturnType(symbolTable: SymbolTable, depth: Int): Expression {
        return getExpressionFromList(symbolTable, null, allExprs, depth)
    }

    fun getExpressionWithReturnType(symbolTable: SymbolTable, returnType: WGSLType, depth: Int): Expression {
        val exprs = (when (returnType.type) {
            Type.BOOL  -> TypeExprs.BOOL
            Type.FLOAT -> TypeExprs.FLOAT
            Type.INT   -> TypeExprs.INT
            Type.UNINT -> TypeExprs.UNINT
            else       -> throw Exception("Attempt to generate Expression with unknown returnType $returnType!")
        }).typeExprs
        return getExpressionFromList(symbolTable, returnType, exprs, depth)
    }

    private fun getExpressionFromList(
        symbolTable: SymbolTable, givenReturnType: WGSLType?, exprs: ArrayList<Expr>, depth: Int
    ): Expression {
        val possibleExprs: ArrayList<Expr> = if (depth >= CNFG.maxExpressionRecursion) {
            ArrayList(IdentityExpr.values().asList())
        } else {
            exprs
        }

        val expr = PRNG.getRandomExprFrom(possibleExprs)
        val exprType = ExprTypes.typeOf(expr)
        val returnType = givenReturnType ?: PRNG.getRandomTypeFrom(exprType.exprTypes)

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