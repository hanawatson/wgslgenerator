package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.PRNG

internal class BuiltinExpression : Expression() {
    private var argsLimit = 0
    private val args = ArrayList<Expression>()
    private val argTypes = ArrayList<WGSLType>()

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): BuiltinExpression {
        this.returnType = returnType
        this.expr = expr

        if (expr !is BuiltinExpr) {
            throw Exception("Failure to validate BuiltinExpr during BuiltinExpression generation!")
        }
        argsLimit = expr.args - 1
        for (i in (0..argsLimit)) {
            argTypes.add(returnType)
        }

        // set bool/int/unint matching types. must match existing returnType dimensions when vectors are implemented
        var matchingBoolType: WGSLType = WGSLScalarType(Type.BOOL)
        // var matchingIntType: WGSLType = WGSLScalarType(Type.INT)
        // var matchingUnIntType: WGSLType = WGSLScalarType(Type.UNINT)
        if (returnType is WGSLVectorType) {
            matchingBoolType = WGSLVectorType(matchingBoolType as WGSLScalarType, returnType.length)
            // matchingIntType = WGSLVectorType(matchingIntType as WGSLScalarType, returnType.length)
            // matchingUnIntType = WGSLVectorType(matchingUnIntType as WGSLScalarType, 0)
        }

        var matchingVectorType: WGSLType = abstractWGSLVectorType
        if (returnType is WGSLScalarType) {
            matchingVectorType = WGSLVectorType(returnType, 0)
        }

        // allow for irregular/different type args
        when (expr) {
            BuiltinArithmeticScalarExpr.DOT                -> {
                argTypes[0] = PRNG.getRandomTypeFrom(arrayListOf(matchingVectorType))
                argTypes[1] = argTypes[0]
            }
            /*BuiltinFloatExpr.LDEXP                         -> {
                argTypes[1] = matchingIntType
            }*/
            BuiltinFloatExpr.MIX                           -> {
                // cover both the linear and component versions of the mix function
                argTypes[2] = PRNG.getRandomTypeFrom(arrayListOf(scalarFloatType, returnType))
            }
            BuiltinFloatScalarExpr.DISTANCE                -> {
                argTypes[0] = PRNG.getRandomTypeFrom(arrayListOf(scalarFloatType, vectorFloatType))
                argTypes[1] = argTypes[0]
            }
            BuiltinFloatScalarExpr.LENGTH                  -> {
                argTypes[0] = PRNG.getRandomTypeFrom(arrayListOf(scalarFloatType, vectorFloatType))
            }
            /*BuiltinFloatVectorExpr.REFRACT                 -> {
                argTypes[2] = scalarFloatType
            }*/
            BuiltinGeneralExpr.SELECT                      -> {
                argTypes[2] = matchingBoolType
            }
            BuiltinIntegerExpr.EXTRACT_BITS                -> {
                argTypes[1] = scalarUnIntType
                argTypes[2] = scalarUnIntType
            }
            BuiltinIntegerExpr.INSERT_BITS                 -> {
                argTypes[2] = scalarUnIntType
                argTypes[3] = scalarUnIntType
            }
            /*BuiltinIntegerExpr.SHIFT_LEFT -> {
                argTypes[1] = matchingUnIntType
            }
            BuiltinIntegerExpr.SHIFT_RIGHT -> {
                argTypes[1] = matchingUnIntType
            }*/

            // temporarily restricted to vectorBools only due to nonfunctional implementation
            // acting on scalarBools in naga
            BuiltinLogicalExpr.ALL, BuiltinLogicalExpr.ANY -> {
                argTypes[0] = PRNG.getRandomTypeFrom(arrayListOf(/*scalarBoolType, */vectorBoolType))
            }
        }

        for (i in (0..argsLimit)) {
            val argExpression = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argTypes[i], depth + 1)
            args.add(argExpression)
        }

        return this
    }

    override fun toString(): String {
        var funcString = "${expr.operator}(${args[0]}"
        for (i in 1..argsLimit) {
            funcString += ", ${args[i]}"
        }
        funcString += ")"

        return funcString
    }
}