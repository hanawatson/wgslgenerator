package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.statement.ControlFlowStat.IF
import wgslsmith.wgslgenerator.ast.statement.ControlFlowStat.SWITCH
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class ControlFlowStatement(symbolTable: SymbolTable, override var stat: Stat, depth: Int) : Statement {
    private val statement: Statement

    init {
        if (stat !is ControlFlowStat) {
            throw Exception("Failure to validate ControlFlowStat during ControlFlowStatement generation!")
        }
        statement = when (stat as ControlFlowStat) {
            IF     -> IfStatement(symbolTable, stat, depth)
            SWITCH -> SwitchStatement(symbolTable, stat, depth)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        /*return when (stat as ControlFlowStat) {
            IF     -> IfStatement().getTabbedLines()
            SWITCH -> SwitchStatement().getTabbedLines()
        }*/
        return statement.getTabbedLines()
    }
}