package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class LoopStatement(
    symbolTable: SymbolTable, override var stat: Stat, depth: Int, inLoop: Boolean, inFunction: Boolean
) : Statement {
    private var safeIteratorSymbol: Symbol? = null
    private val loopBody: ScopeBody
    private var continuingBody: ScopeBody? = null
    private var breakIfCond: Expression? = null

    init {
        if (CNFG.ensureLoopTermination) {
            safeIteratorSymbol = symbolTable.addNewNonWriteableSymbol(Symbol("loop$iteratorLabel", scalarIntType))
            iteratorLabel++
        }

        val loopSymbolTable = symbolTable.copy()
        loopBody = ScopeBody(loopSymbolTable, ScopeState.LOOP, depth + 1, inLoop = true, inFunction)
        if (PRNG.eval(CNFG.generateContinuingBlock)) {
            val continuingSymbolTable = if (CNFG.ensureContinueIsValid) symbolTable.copy() else loopSymbolTable
            continuingSymbolTable.newVarLabelIndex = loopSymbolTable.newVarLabelIndex
            ScopeBody(continuingSymbolTable, ScopeState.NONE, depth + 1, inLoop, inFunction)
            if (PRNG.eval(CNFG.generateContinuingBreakIfStatement)) {
                breakIfCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, scalarBoolType, 0)
            }
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        val loopLines = ArrayList<String>()

        if (CNFG.ensureLoopTermination) {
            val loopIteratorInitial = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            val loopIteratorLimit = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            loopLines.add("var ${safeIteratorSymbol!!.name} = $loopIteratorInitial;")
            loopLines.add("loop {")
            loopLines.add("\tif ${safeIteratorSymbol!!.name} >= $loopIteratorLimit { break; }")
        } else {
            loopLines.add("loop {")
        }
        loopLines.addAll(loopBody.getTabbedLines())

        if (continuingBody != null) {
            loopLines.add("\tcontinuing {")
            if (CNFG.ensureLoopTermination) loopLines.add("\t\t${safeIteratorSymbol!!.name}++;")
            for (continuingBodyLine in continuingBody!!.getTabbedLines()) {
                loopLines.add("\t" + continuingBodyLine)
            }
            if (breakIfCond != null) {
                loopLines.add("\t\tbreak if $breakIfCond;")
            }
            loopLines.add("\t}")
        }

        loopLines.add("}")
        return loopLines
    }

    companion object : StatementCompanion {
        var iteratorLabel = 0

        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return allTypes
        }
    }
}