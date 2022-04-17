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
            stats.add(ContextSpecificStat.BREAK)
            stats.add(ContextSpecificStat.FALLTHROUGH)
        }

        if (depth >= CNFG.maxStatementRecursion) {
            stats = assignStats
        }

        return when (val stat = PRNG.getRandomStatFrom(stats)) {
            is AssignStat          -> AssignmentStatement().generate(symbolTable, stat, depth)
            is ContextSpecificStat -> ContextSpecificStatement().generate(symbolTable, stat, depth)
            is ControlFlowStat     -> ControlFlowStatement().generate(symbolTable, stat, depth)
            else                   -> throw Exception("Attempt to generate Statement with uncategorized Stat $stat!")
        }
    }

    fun getContextSpecificStatement(symbolTable: SymbolTable, contextSpecificStat: ContextSpecificStat, depth: Int):
            Statement {
        return ContextSpecificStatement().generate(symbolTable, contextSpecificStat, depth)
    }
}

internal abstract class Statement {
    abstract var stat: Stat

    abstract fun getTabbedLines(): ArrayList<String>
    abstract fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): Statement
}