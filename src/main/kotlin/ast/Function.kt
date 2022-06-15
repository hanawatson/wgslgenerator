package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.expression.ExprTypes
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class Function(moduleSymbolTable: SymbolTable, funcLabelIndex: Int) {
    val functionName = "func$funcLabelIndex"
    val params = ArrayList<Symbol>()
    private val body: ScopeBody
    private val returnType: WGSLType
    private val returnExpression: Expression

    init {
        val symbolTable = moduleSymbolTable.copy()

        // for now, only generate functions that return a value
        returnType = PRNG.getRandomTypeFrom(allTypes)

        var currentParams = 0
        while (PRNG.eval(CNFG.generateFunctionParam) && currentParams < CNFG.maxFunctionParams) {
            val paramType = PRNG.getRandomTypeFrom(allTypes)
            val param = Symbol("param$currentParams", paramType)

            currentParams++
            params.add(param)
            symbolTable.addNewNonWriteableSymbol(param)
        }

        body = ScopeBody(symbolTable, ScopeState.NONE, 0, inFunction = true)
        returnExpression = ExpressionGenerator.getExpressionWithReturnType(symbolTable, returnType, 0)

        FunctionRegistry.registerFunction(this, returnType)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("fn $functionName(")

        val lastParamIndex = params.size - 1
        for (i in 0 until lastParamIndex) {
            stringBuilder.append("${params[i]}: ${params[i].type}, ")
        }
        if (lastParamIndex >= 0) {
            stringBuilder.append("${params[lastParamIndex]}: ${params[lastParamIndex].type}")
        }

        stringBuilder.append(") -> $returnType {\n")

        for (bodyLine in body.getTabbedLines()) {
            stringBuilder.append("$bodyLine\n")
        }

        stringBuilder.append("\treturn $returnExpression;\n")
        stringBuilder.append("}\n")

        return stringBuilder.toString()
    }
}

internal object FunctionRegistry {
    var noOfFuncs = 0
    private val funcMap = HashMap<WGSLType, ArrayList<Function>>()

    fun registerFunction(function: Function, returnType: WGSLType) {
        // indicate that some function with this return type exists/can be called
        ExprTypes.registerFuncReturnType(returnType)

        if (funcMap[returnType] == null) {
            funcMap[returnType] = arrayListOf(function)
        } else {
            funcMap[returnType]!! += function
        }

        noOfFuncs++
    }

    fun getFunctionReturning(returnType: WGSLType): ArrayList<Function>? {
        return funcMap[returnType]
    }
}