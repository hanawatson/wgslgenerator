package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.CNFG.nagaSafe
import wgslsmith.wgslgenerator.utils.PRNG

internal class BuiltinExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private val args = ArrayList<Expression>()

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        val argTypeList = PRNG.getRandomTypeListFrom(argsForExprType(expr, returnType))
        val concreteArgTypeList = ArrayList<WGSLType>()
        for (i in 0 until argTypeList.size) {
            var concreteArgType = PRNG.getRandomTypeFrom(arrayListOf(argTypeList[i]))
            for (j in 0 until i) {
                if (argTypeList[j] == argTypeList[i]) {
                    concreteArgType = concreteArgTypeList[j]
                }
                break
            }
            concreteArgTypeList.add(concreteArgType)

            val argExpression = ExpressionGenerator.getExpressionWithReturnType(symbolTable, concreteArgType, depth + 1)
            args.add(argExpression)
        }
    }

    override fun toString(): String {
        var builtinExpressionString = "${expr.operator}(${args[0]}"
        for (i in 1 until args.size) {
            builtinExpressionString += ", ${args[i]}"
        }
        builtinExpressionString += ")"

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                builtinExpressionString = "($builtinExpressionString)"
            }
        }

        return builtinExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<ArrayList<WGSLType>> {
            if (expr !is BuiltinExpr) {
                throw Exception("Failure to validate BuiltinExpr during BuiltinExpression argType generation!")
            }

            // return lists of combinations of possible arguments
            val argTypeLists = ArrayList<ArrayList<WGSLType>>()

            var matchingBoolType: WGSLType = WGSLScalarType(Type.BOOL)
            var matchingIntType: WGSLType = WGSLScalarType(Type.INT)
            var matchingUnIntType: WGSLType = WGSLScalarType(Type.UNINT)
            if (returnType is WGSLVectorType) {
                matchingBoolType = WGSLVectorType(matchingBoolType as WGSLScalarType, returnType.length)
                matchingIntType = WGSLVectorType(matchingIntType as WGSLScalarType, returnType.length)
                matchingUnIntType = WGSLVectorType(matchingUnIntType as WGSLScalarType, 0)
            }
            var matchingVectorType: WGSLType = abstractWGSLVectorType
            if (returnType is WGSLScalarType) {
                matchingVectorType = WGSLVectorType(returnType, 0)
            }
            val matchingSquareMatrixTypes = ArrayList<WGSLType>()
            if (returnType is WGSLScalarType) {
                for (i in 2..4) {
                    matchingSquareMatrixTypes.add(WGSLMatrixType(returnType, i, i))
                }
            }
            var transposeMatrixType: WGSLType = abstractWGSLMatrixType
            if (returnType is WGSLMatrixType) {
                transposeMatrixType = WGSLMatrixType(returnType.componentType, returnType.length, returnType.width)
            }

            when (expr) {
                BuiltinArithmeticScalarExpr.DOT                -> {
                    argTypeLists.add(arrayListOf(matchingVectorType, matchingVectorType))
                }
                BuiltinFloatExpr.LDEXP                         -> {
                    argTypeLists.add(arrayListOf(returnType, matchingIntType))
                }
                BuiltinFloatExpr.MIX                           -> {
                    // cover both the linear and component versions of the mix function
                    argTypeLists.add(arrayListOf(returnType, returnType, scalarFloatType))
                    argTypeLists.add(arrayListOf(returnType, returnType, returnType))
                }
                BuiltinFloatScalarExpr.DETERMINANT             -> {
                    for (matchingSquareMatrixType in matchingSquareMatrixTypes) {
                        argTypeLists.add(arrayListOf(matchingSquareMatrixType))
                    }
                }
                BuiltinFloatScalarExpr.DISTANCE                -> {
                    argTypeLists.add(arrayListOf(scalarFloatType, scalarFloatType))
                    argTypeLists.add(arrayListOf(vectorFloatType, vectorFloatType))
                }
                BuiltinFloatScalarExpr.LENGTH                  -> {
                    argTypeLists.add(arrayListOf(scalarFloatType))
                    argTypeLists.add(arrayListOf(vectorFloatType))
                }
                BuiltinFloatVectorExpr.REFRACT                 -> {
                    argTypeLists.add(arrayListOf(returnType, returnType, scalarFloatType))
                }
                BuiltinGeneralExpr.SELECT                      -> {
                    argTypeLists.add(arrayListOf(returnType, returnType, scalarBoolType))
                    if (matchingBoolType != scalarBoolType) {
                        argTypeLists.add(arrayListOf(returnType, returnType, matchingBoolType))
                    }
                }
                BuiltinIntegerExpr.EXTRACT_BITS                -> {
                    argTypeLists.add(arrayListOf(returnType, scalarUnIntType, scalarUnIntType))
                }
                BuiltinIntegerExpr.INSERT_BITS                 -> {
                    argTypeLists.add(arrayListOf(returnType, returnType, scalarUnIntType, scalarUnIntType))
                }
                BuiltinIntegerExpr.SHIFT_LEFT                  -> {
                    argTypeLists.add(arrayListOf(returnType, matchingUnIntType))
                }
                BuiltinIntegerExpr.SHIFT_RIGHT                 -> {
                    argTypeLists.add(arrayListOf(returnType, matchingUnIntType))
                }
                // restrict to vectorBools only due to nonfunctional implementation acting on scalarBools in naga
                BuiltinLogicalExpr.ALL, BuiltinLogicalExpr.ANY -> {
                    if (!nagaSafe) argTypeLists.add(arrayListOf(scalarBoolType))
                    argTypeLists.add(arrayListOf(vectorBoolType))
                }
                BuiltinMatrixExpr.TRANSPOSE                    -> {
                    argTypeLists.add(arrayListOf(transposeMatrixType))
                }
                else                                           -> {
                    argTypeLists.add(ArrayList(generateSequence { returnType }.take(expr.args).toList()))
                }
            }

            return argTypeLists
        }
    }
}