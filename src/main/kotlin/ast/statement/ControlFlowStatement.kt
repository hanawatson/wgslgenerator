package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.statement.ControlFlowStat.IF
import wgslsmith.wgslgenerator.ast.statement.ControlFlowStat.SWITCH
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class ControlFlowStatement : Statement {
    override lateinit var stat: Stat

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): Statement {
        this.stat = stat

        if (stat !is ControlFlowStat) {
            throw Exception("Failure to validate ControlFlowStat during ControlFlowStatement generation!")
        }

        return when (stat) {
            IF     -> IfStatement().generate(symbolTable, stat, depth)
            SWITCH -> SwitchStatement().generate(symbolTable, stat, depth)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        return when (stat as ControlFlowStat) {
            IF     -> IfStatement().getTabbedLines()
            SWITCH -> SwitchStatement().getTabbedLines()
        }
    }
}