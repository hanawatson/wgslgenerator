package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.CNFG.prob
import wgslsmith.wgslgenerator.utils.PRNG

internal class ForStatement(symbolTable: SymbolTable, override var stat: Stat, depth: Int) : Statement {
    private var initSymbol: Symbol? = null
    private var initStatement: Statement? = null
    private var condExpression: Expression? = null
    private var updateStatement: Statement? = null
    private val forBody: ScopeBody

    init {
        val forSymbolTable = symbolTable.copy()

        if (CNFG.ensureForLoopTermination) {
            initSymbol = forSymbolTable.addNewNonWriteableSymbol(Symbol("for$iteratorLabel", scalarIntType))
            iteratorLabel++
        } else {
            if (!PRNG.eval(CNFG.omitForLoopInitializer)) {
                initStatement = AssignmentStatement(
                    forSymbolTable, AssignmentEqStat.ASSIGN_SIMPLE, mustBeNewSymbol = true
                )
            }
            if (!PRNG.eval(CNFG.omitForLoopCondition) && prob(scalarBoolType) > 0.0) {
                condExpression = ExpressionGenerator.getExpressionWithReturnType(forSymbolTable, scalarBoolType, 0)
            }
            if (!PRNG.eval(CNFG.omitForLoopUpdate) && forSymbolTable.hasWriteableOfAny(allConcreteTypes)) {
                val possibleStats = arrayListOf<Stat>(AssignmentEqStat.ASSIGN_SIMPLE)
                if (forSymbolTable.hasWriteableOfAny(compoundAssignableConcreteTypes)) {
                    possibleStats.add(AssignmentCompoundStat.BINARY_OPERATOR)
                }
                if (forSymbolTable.hasWriteableOfAny(arrayListOf(scalarIntType, scalarUnIntType))) {
                    possibleStats.add(AssignmentCompoundStat.DECREMENT)
                    possibleStats.add(AssignmentCompoundStat.INCREMENT)
                }

                val updateStat = PRNG.getRandomStatFrom(possibleStats)
                updateStatement = AssignmentStatement(forSymbolTable, updateStat, mustBeExistingSymbol = true)
            }
        }
        forBody = ScopeBody(forSymbolTable, ScopeState.LOOP, depth + 1, inLoop = true)
    }

    override fun getTabbedLines(): ArrayList<String> {
        val forLines = ArrayList<String>()

        if (CNFG.ensureForLoopTermination) {
            val forIteratorInitial = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            val forIteratorLimit = PRNG.getRandomIntInRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
            val i = initSymbol!!.name
            forLines.add("for(var $i = $forIteratorInitial; $i < $forIteratorLimit; $i++) {")
        } else {
            val initString = if (initStatement != null) {
                initStatement!!.getTabbedLines()[0].removeSuffix(";")
            } else ""
            val condString = if (condExpression != null) "$condExpression" else ""
            val updateString = if (updateStatement != null) {
                updateStatement!!.getTabbedLines()[0].removeSuffix(";")
            } else ""
            forLines.add("for($initString; $condString; $updateString) {")
        }
        forLines.addAll(forBody.getTabbedLines())
        forLines.add("}")

        return forLines
    }

    companion object : StatementCompanion {
        var iteratorLabel = 0

        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return allTypes
        }
    }
}