package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type.*
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType

internal enum class ExprTypes(val exprTypes: ArrayList<WGSLType>, val exprs: List<Expr>) {
    BINARY_ARITHMETIC(
        arrayListOf(WGSLScalarType(FLOAT), WGSLScalarType(INT), WGSLScalarType(UNINT)), BinaryArithmeticExpr.values()
            .asList()
    ),
    BINARY_BIT(arrayListOf(WGSLScalarType(INT), WGSLScalarType(UNINT)), BinaryBitExpr.values().asList()),
    BINARY_LOGICAL(arrayListOf(WGSLScalarType(BOOL)), BinaryLogicalExpr.values().asList()),
    BUILTIN_ARITHMETIC(
        arrayListOf(WGSLScalarType(FLOAT), WGSLScalarType(INT), WGSLScalarType(UNINT)),
        BuiltinArithmeticExpr.values().asList()
    ),
    BUILTIN_FLOAT(arrayListOf(WGSLScalarType(FLOAT)), BuiltinFloatExpr.values().asList()),
    BUILTIN_INTEGER(arrayListOf(WGSLScalarType(INT), WGSLScalarType(UNINT)), BuiltinIntegerExpr.values().asList()),
    BUILTIN_LOGICAL(arrayListOf(WGSLScalarType(BOOL)), BuiltinLogicalExpr.values().asList()),
    COMPARISON_EQ(arrayListOf(WGSLScalarType(BOOL)), ComparisonEqExpr.values().asList()),
    COMPARISON_TH(arrayListOf(WGSLScalarType(BOOL)), ComparisonThExpr.values().asList()),
    IDENTITY(arrayListOf(WGSLScalarType(BOOL), WGSLScalarType(FLOAT)), IdentityExpr.values().asList()),
    UNARY_ARITHMETIC(arrayListOf(WGSLScalarType(FLOAT), WGSLScalarType(INT)), UnaryArithmeticExpr.values().asList()),
    UNARY_BIT(arrayListOf(WGSLScalarType(INT), WGSLScalarType(UNINT)), UnaryBitExpr.values().asList()),
    UNARY_LOGICAL(arrayListOf(WGSLScalarType(BOOL)), UnaryLogicalExpr.values().asList());

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
                    ExprTypes.COMPARISON_EQ.exprs +
                    ExprTypes.COMPARISON_TH.exprs +
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