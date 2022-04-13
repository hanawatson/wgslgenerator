package wgslsmith.wgslgenerator.internalProgRep

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.Type.*

internal enum class ExprTypes(val exprTypes: ArrayList<Type>, val exprs: List<Expr>) {
    BINARY_ARITHMETIC(arrayListOf(FLOAT, INT, UNINT), BinaryArithmeticExpr.values().asList()),
    BINARY_BIT(arrayListOf(INT, UNINT), BinaryBitExpr.values().asList()),
    BINARY_LOGICAL(arrayListOf(BOOL), BinaryLogicalExpr.values().asList()),
    BUILTIN_ARITHMETIC(arrayListOf(FLOAT, INT, UNINT), BuiltinArithmeticExpr.values().asList()),
    BUILTIN_FLOAT(arrayListOf(FLOAT), BuiltinFloatExpr.values().asList()),
    BUILTIN_INTEGER(arrayListOf(INT, UNINT), BuiltinIntegerExpr.values().asList()),
    BUILTIN_LOGICAL(arrayListOf(BOOL), BuiltinLogicalExpr.values().asList()),
    COMPARISON(arrayListOf(BOOL), ComparisonExpr.values().asList()),
    IDENTITY(ArrayList(Type.values().asList()), IdentityExpr.values().asList()),
    UNARY_ARITHMETIC(arrayListOf(FLOAT, INT), UnaryArithmeticExpr.values().asList()),
    UNARY_BIT(arrayListOf(INT, UNINT), UnaryBitExpr.values().asList()),
    UNARY_LOGICAL(arrayListOf(BOOL), UnaryLogicalExpr.values().asList());

    companion object {
        fun typeOf(expr: Expr): ExprTypes {
            for (exprType in ExprTypes.values()) {
                if (exprType.exprs.contains(expr)) {
                    return exprType
                }
            }

            throw Exception("Attempt to use Type without corresponding ExprType!")
        }
    }
}

internal val allExprs = ExprTypes.values().fold(ArrayList()) { acc: ArrayList<Expr>, exprType: ExprTypes ->
    ArrayList(acc + exprType.exprs)
}

internal enum class TypeExprs(val typeExprs: ArrayList<Expr>) {
    BOOL(
        ArrayList(
            ExprTypes.BINARY_LOGICAL.exprs +
                    ExprTypes.BUILTIN_LOGICAL.exprs +
                    ExprTypes.COMPARISON.exprs +
                    ExprTypes.IDENTITY.exprs +
                    ExprTypes.UNARY_LOGICAL.exprs
        )
    ),
    FLOAT(
        ArrayList(
            ExprTypes.BINARY_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_FLOAT.exprs +
                    ExprTypes.IDENTITY.exprs +
                    ExprTypes.UNARY_ARITHMETIC.exprs
        )
    ),
    INT(
        ArrayList(
            ExprTypes.BINARY_ARITHMETIC.exprs +
                    ExprTypes.BINARY_BIT.exprs +
                    ExprTypes.BUILTIN_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_INTEGER.exprs +
                    ExprTypes.IDENTITY.exprs +
                    ExprTypes.UNARY_ARITHMETIC.exprs +
                    ExprTypes.UNARY_BIT.exprs
        )
    ),
    UNINT(
        ArrayList(
            ExprTypes.BINARY_ARITHMETIC.exprs +
                    ExprTypes.BINARY_BIT.exprs +
                    ExprTypes.BUILTIN_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_INTEGER.exprs +
                    ExprTypes.IDENTITY.exprs +
                    ExprTypes.UNARY_BIT.exprs
        )
    );
}