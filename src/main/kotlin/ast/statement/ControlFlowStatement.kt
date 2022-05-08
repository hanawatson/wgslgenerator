package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.statement.ControlFlowStat.*
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class ControlFlowStatement(
    symbolTable: SymbolTable, override var stat: Stat, depth: Int, inLoop: Boolean = false
) : Statement {
    private val statement: Statement

    init {
        if (stat !is ControlFlowStat) {
            throw Exception("Failure to validate ControlFlowStat during ControlFlowStatement generation!")
        }
        statement = when (stat as ControlFlowStat) {
            FOR    -> ForStatement(symbolTable, stat, depth)
            IF     -> IfStatement(symbolTable, stat, depth, inLoop)
            LOOP   -> LoopStatement(symbolTable, stat, depth, inLoop)
            SWITCH -> SwitchStatement(symbolTable, stat, depth, inLoop)
            WHILE  -> WhileStatement(symbolTable, stat, depth)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        return statement.getTabbedLines()
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return when (stat) {
                IF     -> IfStatement.usedTypes(stat)
                LOOP   -> LoopStatement.usedTypes(stat)
                SWITCH -> SwitchStatement.usedTypes(stat)
                else   -> arrayListOf()
            }
        }
    }
}