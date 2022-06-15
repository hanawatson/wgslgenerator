package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.CNFG.nagaSafe
import wgslsmith.wgslgenerator.utils.CNFG.tintSafe

internal enum class ExprTypes(val types: ArrayList<WGSLType>, val exprs: List<Expr>, val areConsts: Boolean = false) {
    ACCESS_CONVENIENCE(
        arrayListOf(abstractWGSLScalarType, abstractWGSLVectorType),
        AccessConvenienceExpr.values().asList(),
    ),
    ACCESS_SUBSCRIPT(
        arrayElementTypes,
        AccessSubscriptExpr.values().asList()
    ),
    BINARY_ARITHMETIC_NUMERIC(
        numericTypes,
        BinaryArithmeticNumericExpr.values().asList()
    ),
    BINARY_ARITHMETIC_MATRIX_NUMERIC(
        ArrayList(numericTypes + arrayListOf(abstractWGSLMatrixType)),
        BinaryArithmeticMatrixNumericExpr.values().asList()
    ),
    BINARY_BIT(
        arrayListOf(scalarIntType, scalarUnIntType, vectorIntType, vectorUnIntType),
        BinaryBitExpr.values().asList()
    ),
    BINARY_LOGICAL(
        arrayListOf(scalarBoolType, vectorBoolType),
        BinaryLogicalExpr.values().asList()
    ),
    BINARY_LOGICAL_SCALAR(
        arrayListOf(scalarBoolType),
        BinaryLogicalScalarExpr.values().asList()
    ),
    BUILTIN_ARITHMETIC(
        numericTypes,
        BuiltinArithmeticExpr.values().asList()
    ),
    BUILTIN_ARITHMETIC_SCALAR(
        arrayListOf(scalarFloatType, scalarIntType, scalarUnIntType),
        BuiltinArithmeticScalarExpr.values().asList()
    ),
    BUILTIN_FLOAT(
        arrayListOf(scalarFloatType, vectorFloatType),
        BuiltinFloatExpr.values().asList()
    ),
    BUILTIN_FLOAT_SCALAR(
        arrayListOf(scalarFloatType),
        BuiltinFloatScalarExpr.values().asList()
    ),
    BUILTIN_FLOAT_VECTOR(
        arrayListOf(vectorFloatType),
        BuiltinFloatVectorExpr.values().asList()
    ),
    BUILTIN_FLOAT_VECTOR3(
        arrayListOf(vector3FloatType),
        BuiltinFloatVector3Expr.values().asList()
    ),
    BUILTIN_GENERAL(
        arrayListOf(abstractWGSLScalarType, abstractWGSLVectorType),
        BuiltinGeneralExpr.values().asList()
    ),
    BUILTIN_INTEGER(
        arrayListOf(scalarIntType, scalarUnIntType, vectorIntType, vectorUnIntType),
        BuiltinIntegerExpr.values().asList()
    ),
    BUILTIN_LOGICAL(
        arrayListOf(scalarBoolType),
        BuiltinLogicalExpr.values().asList()
    ),
    BUILTIN_MATRIX(
        arrayListOf(abstractWGSLMatrixType),
        BuiltinMatrixExpr.values().asList()
    ),
    COMPARISON_EQ(
        arrayListOf(scalarBoolType),
        ComparisonEqExpr.values().asList()
    ),
    COMPARISON_TH(
        arrayListOf(scalarBoolType),
        ComparisonThExpr.values().asList()
    ),
    CONVERSION_BITCAST(
        numericTypes,
        ConversionBitcastExpr.values().asList()
    ),
    CONVERSION_GENERAL(
        arrayListOf(abstractWGSLScalarType, abstractWGSLVectorType),
        ConversionGeneralExpr.values().asList()
    ),
    DATA_PACK(
        arrayListOf(scalarUnIntType),
        DataPackExpr.values().asList()
    ),
    DATA_UNPACK(
        arrayListOf(vector2FloatType, vector4FloatType),
        DataUnpackExpr.values().asList()
    ),
    IDENTITY_COMPOSITE(
        compositeTypes,
        IdentityCompositeExpr.values().asList(),
        areConsts = true
    ),
    IDENTITY_SCALAR(
        scalarTypes,
        IdentityScalarExpr.values().asList(),
        areConsts = true
    ),
    IDENTITY_UNIVERSAL(
        allTypes,
        IdentityUniversalExpr.values().asList(),
        // disable due to Tint and naga support lack of SYMBOL for consts, naga of ZERO_VALUE
        areConsts = !nagaSafe && !tintSafe
    ),
    UNARY_ARITHMETIC(
        arrayListOf(scalarFloatType, scalarIntType, vectorFloatType, vectorIntType),
        UnaryArithmeticExpr.values().asList()
    ),
    UNARY_BIT(
        arrayListOf(scalarIntType, scalarUnIntType, vectorIntType, vectorUnIntType),
        UnaryBitExpr.values().asList()
    ),
    UNARY_LOGICAL(
        arrayListOf(scalarBoolType, vectorBoolType),
        UnaryLogicalExpr.values().asList()
    );

    companion object {
        private val funcReturnTypes = ArrayList<WGSLType>()

        fun registerFuncReturnType(type: WGSLType) {
            funcReturnTypes.add(type)
        }

        fun exprTypes(expr: Expr): ArrayList<WGSLType> {
            if (expr is FunctionExpr) {
                return funcReturnTypes
            }

            for (exprType in ExprTypes.values()) {
                if (exprType.exprs.contains(expr)) {
                    return exprType.types
                }
            }

            throw Exception("Attempt to retrieve ExprTypes types of unknown Expr $expr!")
        }

        fun getExprs(returnType: WGSLType, consts: Boolean = false): ArrayList<Expr> {
            val exprs = ArrayList<Expr>()

            for (exprType in ExprTypes.values()) {
                if (!consts || exprType.areConsts) {
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
            }

            if (returnType in funcReturnTypes) {
                exprs.add(FunctionExpr.FUNCTION)
            }

            return exprs
        }
    }
}