package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable

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
        val matchingBoolType = WGSLScalarType(Type.BOOL)
        // val matchingIntType = WGSLScalarType(Type.INT)
        // val matchingUnIntType = WGSLScalarType(Type.UNINT)

        val scalarFloatType = WGSLScalarType(Type.FLOAT)
        val scalarUnIntType = WGSLScalarType(Type.UNINT)

        // allow for irregular/different type args
        when (expr) {
            /*BuiltinFloatExpr.LDEXP          -> {
                argTypes[1] = matchingIntType
            }*/
            BuiltinFloatExpr.MIX_COMPONENT  -> {
                argTypes[2] = scalarFloatType
            }
            BuiltinIntegerExpr.EXTRACT_BITS -> {
                argTypes[1] = scalarUnIntType
                argTypes[2] = scalarUnIntType
            }
            BuiltinIntegerExpr.INSERT_BITS  -> {
                argTypes[2] = scalarUnIntType
                argTypes[3] = scalarUnIntType
            }
            /*BuiltinIntegerExpr.SHIFT_LEFT -> {
                argTypes[1] = matchingUnIntType
            }
            BuiltinIntegerExpr.SHIFT_RIGHT -> {
                argTypes[1] = matchingUnIntType
            }*/
            BuiltinLogicalExpr.SELECT       -> {
                argTypes[2] = matchingBoolType
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