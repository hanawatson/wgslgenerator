package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.CNFG.omitTypeFromCompositeConstruction
import wgslsmith.wgslgenerator.utils.PRNG

internal object ModuleScope {
    private val consts = ArrayList<Pair<ConstSymbol, Expression>>()
    private var newConstLabelIndex = 0

    fun generateNewConst(symbolTable: SymbolTable) {
        // select from scalar types only due to issues with the implementation of composite consts in naga
        val type = PRNG.getRandomTypeFrom(scalarTypes)
        val expression = ExpressionGenerator.getConstExpressionWithReturnType(symbolTable, type, 0)
        val const = ConstSymbol("const$newConstLabelIndex", expression.returnType, expression.getConstValue())

        newConstLabelIndex++
        consts.add(Pair(const, expression))
        symbolTable.addNewNonWriteableSymbol(const)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        // ensure types are printed during composite construction due to lack of support in naga for composite consts
        // without explicit types
        val omitTypeFromCompositeConstructionOriginal = omitTypeFromCompositeConstruction
        omitTypeFromCompositeConstruction = 0.0
        for (const in consts) {
            val declMethod = if (PRNG.eval(CNFG.declareConstWithLet)) "let" else "var<private>"
            stringBuilder.append("$declMethod ${const.first}: ${const.first.type} = ${const.second};\n")
        }
        omitTypeFromCompositeConstruction = omitTypeFromCompositeConstructionOriginal

        return stringBuilder.toString()
    }
}