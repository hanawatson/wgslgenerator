package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class WhileStatement(
    symbolTable: SymbolTable, override var stat: Stat, depth: Int, inFunction: Boolean
) : Statement {
    private var safeIteratorSymbol: Symbol? = null
    private var whileCond: Expression? = null
    private val whileBody: ScopeBody

    init {
        if (CNFG.ensureWhileLoopTermination) {
            safeIteratorSymbol = symbolTable.addNewNonWriteableSymbol(Symbol("while$iteratorLabel", scalarIntType))
            iteratorLabel++
        } else {
            whileCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, scalarBoolType, 0)
        }
        whileBody = ScopeBody(symbolTable.copy(), ScopeState.LOOP, depth + 1, inLoop = true, inFunction)
    }

    override fun getTabbedLines(): ArrayList<String> {
        val whileLines = ArrayList<String>()

        if (CNFG.ensureWhileLoopTermination) {
            val whileIteratorInitial = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            val whileIteratorLimit = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            whileLines.add("var ${safeIteratorSymbol!!.name} = $whileIteratorInitial;")
            whileLines.add("while ${safeIteratorSymbol!!.name} < $whileIteratorLimit {")
            whileLines.add("\t${safeIteratorSymbol!!.name}++;")
        } else {
            whileLines.add("while $whileCond {")
        }
        whileLines.addAll(whileBody.getTabbedLines())
        whileLines.add("}")

        return whileLines
    }

    companion object : StatementCompanion {
        var iteratorLabel = 0

        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return arrayListOf(scalarBoolType)
        }
    }
}