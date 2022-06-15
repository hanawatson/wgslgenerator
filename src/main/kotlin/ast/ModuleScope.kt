package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG.nagaSafe
import wgslsmith.wgslgenerator.utils.CNFG.omitTypeFromCompositeConstruction
import wgslsmith.wgslgenerator.utils.PRNG

internal class ModuleScope() {
    private val consts = ArrayList<Pair<ConstSymbol, Expression>>()
    private var newConstLabelIndex = 0
    val globals = ArrayList<Symbol>()
    private var newGlobalLabelIndex = 0
    private val functions = ArrayList<Function>()
    private var newFunctionLabelIndex = 0

    fun generateNewConst(symbolTable: SymbolTable) {
        // avoid issues with the implementation of composite consts in naga
        val constTypes = if (nagaSafe) scalarTypes else allTypes
        val type = PRNG.getRandomTypeFrom(constTypes)
        val expression = ExpressionGenerator.getConstExpressionWithReturnType(symbolTable, type, 0)
        val const = ConstSymbol("const$newConstLabelIndex", expression.returnType, expression.getConstValue())

        newConstLabelIndex++
        consts.add(Pair(const, expression))
        symbolTable.addNewNonWriteableSymbol(const)
    }

    fun generateNewGlobal(symbolTable: SymbolTable) {
        val globalTypes = allTypes
        val type = PRNG.getRandomTypeFrom(globalTypes)
        val global = Symbol("global$newGlobalLabelIndex", type)

        newGlobalLabelIndex++
        globals.add(global)
        symbolTable.addNewWriteableSymbol(global)
    }

    fun generateNewFunction(symbolTable: SymbolTable) {
        // figure out some way of logging the function. for now just add and print and stuff
        val function = Function(symbolTable, newFunctionLabelIndex)

        newFunctionLabelIndex++
        functions.add(function)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        // ensure types are printed during composite construction due to lack of support in naga for composite consts
        // without explicit types
        val omitTypeFromCompositeConstructionOriginal = omitTypeFromCompositeConstruction
        if (nagaSafe) omitTypeFromCompositeConstruction = 0.0

        stringBuilder.append("// Module scope consts\n")
        for (const in consts) {
            stringBuilder.append("let ${const.first}: ${const.first.type} = ${const.second};\n")
        }

        omitTypeFromCompositeConstruction = omitTypeFromCompositeConstructionOriginal

        stringBuilder.append("\n")

        stringBuilder.append("// Module scope vars\n")
        for (global in globals) {
            stringBuilder.append("var<private> ${global}: ${global.type};\n")
        }

        stringBuilder.append("\n")

        stringBuilder.append("// Functions\n")
        for (function in functions) {
            stringBuilder.append("$function\n")
        }

        return stringBuilder.toString()
    }
}