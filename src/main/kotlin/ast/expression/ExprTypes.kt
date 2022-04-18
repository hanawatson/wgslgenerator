package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.Type.*

internal enum class ExprTypes(val types: ArrayList<WGSLType>, val exprs: List<Expr>) {
    BINARY_ARITHMETIC(numericTypes, BinaryArithmeticExpr.values().asList()),
    BINARY_BIT(arrayListOf(WGSLScalarType(INT), WGSLScalarType(UNINT)), BinaryBitExpr.values().asList()),
    BINARY_LOGICAL(arrayListOf(WGSLScalarType(BOOL)), BinaryLogicalExpr.values().asList()),
    BUILTIN_ARITHMETIC(numericTypes, BuiltinArithmeticExpr.values().asList()),
    BUILTIN_FLOAT(arrayListOf(WGSLScalarType(FLOAT)), BuiltinFloatExpr.values().asList()),
    BUILTIN_INTEGER(arrayListOf(WGSLScalarType(INT), WGSLScalarType(UNINT)), BuiltinIntegerExpr.values().asList()),
    BUILTIN_LOGICAL(arrayListOf(WGSLScalarType(BOOL)), BuiltinLogicalExpr.values().asList()),
    COMPARISON_EQ(arrayListOf(WGSLScalarType(BOOL)), ComparisonEqExpr.values().asList()),
    COMPARISON_TH(arrayListOf(WGSLScalarType(BOOL)), ComparisonThExpr.values().asList()),
    IDENTITY_CONSTRUCTOR(constructibleTypes, IdentityConstructorExpr.values().asList()),
    IDENTITY_LITERAL(scalarTypes, IdentityLiteralExpr.values().asList()),
    IDENTITY_UNIVERSAL(allTypes, IdentityUniversalExpr.values().asList()),
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

        fun getExprs(returnType: WGSLType): ArrayList<Expr> {
            val exprs = ArrayList<Expr>()

            for (exprType in ExprTypes.values()) {
                var matchesType = false
                for (type in exprType.types) {
                    if (returnType == type || returnType.isRepresentedBy(type)) {
                        matchesType = true
                        break
                    }
                }

                if (matchesType) {
                    exprs.addAll(exprType.exprs)
                }
            }

            return exprs
        }
    }
}

// needs a rework to be simplified for future types!
/*internal enum class TypeExprs(val typeExprs: ArrayList<Expr>) {
    BOOL(
        ArrayList(
            ExprTypes.BINARY_LOGICAL.exprs +
                    ExprTypes.BUILTIN_LOGICAL.exprs +
                    ExprTypes.COMPARISON_EQ.exprs +
                    ExprTypes.COMPARISON_TH.exprs +
                    ExprTypes.IDENTITY_LITERAL.exprs +
                    ExprTypes.IDENTITY_UNIVERSAL.exprs +
                    ExprTypes.UNARY_LOGICAL.exprs
        )
    ),
    FLOAT(
        ArrayList(
            ExprTypes.BINARY_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_FLOAT.exprs +
                    ExprTypes.IDENTITY_LITERAL.exprs +
                    ExprTypes.IDENTITY_UNIVERSAL.exprs +
                    ExprTypes.UNARY_ARITHMETIC.exprs
        )
    ),
    INT(
        ArrayList(
            ExprTypes.BINARY_ARITHMETIC.exprs +
                    ExprTypes.BINARY_BIT.exprs +
                    ExprTypes.BUILTIN_ARITHMETIC.exprs +
                    ExprTypes.BUILTIN_INTEGER.exprs +
                    ExprTypes.IDENTITY_LITERAL.exprs +
                    ExprTypes.IDENTITY_UNIVERSAL.exprs +
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
                    ExprTypes.IDENTITY_LITERAL.exprs +
                    ExprTypes.IDENTITY_UNIVERSAL.exprs +
                    ExprTypes.UNARY_BIT.exprs
        )
    );
}*/