package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Function
import wgslsmith.wgslgenerator.ast.FunctionRegistry
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class FunctionExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private val args = ArrayList<Expression>()
    private val function: Function

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        // choose a function of correct return type
        val possibleFunctions = FunctionRegistry.getFunctionReturning(returnType)
            ?: throw Exception("Attempt to select function of returnType $returnType that has not been registered!")
        function = PRNG.getRandomFunctionFrom(possibleFunctions)

        val argTypeList = function.params.map { it.type }
        for (argType in argTypeList) {
            val argExpression = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
            args.add(argExpression)
        }
    }

    override fun toString(): String {
        var functionExpressionString = "${function.functionName}("

        val lastArgIndex = args.size - 1
        for (i in 0 until lastArgIndex) {
            functionExpressionString += "${args[i]}, "
        }
        if (lastArgIndex >= 0) {
            functionExpressionString += "${args[lastArgIndex]}"
        }

        functionExpressionString += ")"

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                functionExpressionString = "($functionExpressionString)"
            }
        }

        return functionExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<ArrayList<WGSLType>> {
            val functions = FunctionRegistry.getFunctionReturning(returnType)
            val functionsParamTypes = ArrayList<ArrayList<WGSLType>>()

            if (functions != null) {
                for (function in functions) {
                    functionsParamTypes.add(ArrayList(function.params.map { it.type }))
                }
            }

            return functionsParamTypes
        }
    }
}