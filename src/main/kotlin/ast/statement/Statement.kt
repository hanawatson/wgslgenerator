package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal object StatementGenerator {
    fun getStatement(symbolTable: SymbolTable, depth: Int, scopeState: ScopeState): Statement {
        var stats = ArrayList<Stat>()
        stats.addAll(allStats)

        if (scopeState == ScopeState.SWITCH) {
            stats.add(ContextSpecificStat.SWITCH_BREAK)
            stats.add(ContextSpecificStat.SWITCH_FALLTHROUGH)
        }

        if (depth >= CNFG.maxStatementNestDepth) {
            stats = assignStats
        }

        return when (val stat = PRNG.getRandomStatFrom(stats)) {
            is AssignmentStat      -> AssignmentStatement(symbolTable, stat)
            is ContextSpecificStat -> ContextSpecificStatement(stat)
            is ControlFlowStat     -> ControlFlowStatement(symbolTable, stat, depth)
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