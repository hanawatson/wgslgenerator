package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat
import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStatement
import wgslsmith.wgslgenerator.ast.statement.Statement
import wgslsmith.wgslgenerator.ast.statement.StatementGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal enum class ScopeState {
    FUNCTION,
    IF,
    LOOP,
    NONE,
    SWITCH;
}

internal class ScopeBody(
    symbolTable: SymbolTable, scopeState: ScopeState, depth: Int,
    var inLoop: Boolean = false, var inFunction: Boolean = false
) {
    private val statements: ArrayList<Statement> = ArrayList()

    fun getLastStatement() = statements.last()

    fun replaceLastStatement(statement: Statement) {
        statements.remove(statements.last())
        statements.add(statement)
    }

    init {
        var currentStatements = 0
        var generateAnotherStatement = true
        val maxStatements = when (scopeState) {
            ScopeState.FUNCTION -> CNFG.maxStatementsInFunctionBody
            ScopeState.IF       -> CNFG.maxStatementsInIfBody
            ScopeState.LOOP     -> CNFG.maxStatementsInLoopBody
            ScopeState.SWITCH   -> CNFG.maxStatementsInSwitchBody
            else                -> CNFG.maxStatementsInBody
        }

        while (generateAnotherStatement && currentStatements < maxStatements) {
            val statement = StatementGenerator.getStatement(symbolTable, scopeState, depth, inLoop, inFunction)
            statements.add(statement)
            currentStatements++

            generateAnotherStatement = if (((statement.stat == ContextSpecificStat.SWITCH_BREAK
                        || (statement is ContextSpecificStatement && inLoop))
                        && CNFG.preventCodeAfterControlFlowInterruption)
                || statement.stat == ContextSpecificStat.SWITCH_FALLTHROUGH) {
                false
            } else {
                PRNG.eval(CNFG.generateStatement)
            }
        }
    }

    fun getTabbedLines(): ArrayList<String> {
        val bodyStrings = ArrayList<String>()
        for (statement in statements) {
            for (line in statement.getTabbedLines()) {
                bodyStrings.add("\t" + line)
            }
        }
        return bodyStrings
    }
}