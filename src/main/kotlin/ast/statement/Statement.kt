package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal object StatementGenerator {
    fun getStatement(symbolTable: SymbolTable, scopeState: ScopeState, depth: Int, inLoop: Boolean): Statement {
        var stats = ArrayList<Stat>()
        stats.addAll(allStats)

        if (inLoop) {
            stats.addAll(
                arrayListOf(
                    ContextSpecificStat.LOOP_BREAK, ContextSpecificStat.LOOP_CONTINUE, ContextSpecificStat.LOOP_RETURN
                )
            )
        }

        if (scopeState == ScopeState.SWITCH) {
            stats.addAll(arrayListOf(ContextSpecificStat.SWITCH_BREAK, ContextSpecificStat.SWITCH_FALLTHROUGH))
        }

        if (depth >= CNFG.maxStatementNestDepth) {
            stats = assignStats
        }

        return when (val stat = PRNG.getRandomStatFrom(stats)) {
            is AssignmentStat      -> AssignmentStatement(symbolTable, stat)
            is ContextSpecificStat -> ContextSpecificStatement(stat)
            is ControlFlowStat     -> ControlFlowStatement(symbolTable, stat, depth, inLoop)
            else                   -> throw Exception("Attempt to generate Statement with uncategorized Stat $stat!")
        }
    }
}

internal interface Statement {
    var stat: Stat

    fun getTabbedLines(): ArrayList<String>
}

internal interface StatementCompanion {
    fun usedTypes(stat: Stat): ArrayList<*>
}