package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat
import wgslsmith.wgslgenerator.ast.statement.Statement
import wgslsmith.wgslgenerator.ast.statement.StatementGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal enum class ScopeState {
    IF,
    NONE,
    SWITCH;
}

internal class ScopeBody(private val scopeState: ScopeState) {
    private val statements: ArrayList<Statement> = ArrayList()

    fun getLastStatement() = statements.last()

    fun replaceLastStatement(statement: Statement) {
        statements.remove(statements.last())
        statements.add(statement)
    }

    fun generate(symbolTable: SymbolTable, depth: Int): ScopeBody {
        var currentStatements = 0
        var generateAnotherStatement = true
        val maxStatements = when (scopeState) {
            ScopeState.IF     -> CNFG.maxStatementsInIfBody
            ScopeState.SWITCH -> CNFG.maxStatementsInSwitchBody
            else              -> CNFG.maxStatementsInBody
        }

        while (generateAnotherStatement && currentStatements < maxStatements) {
            val statement = StatementGenerator.getStatement(symbolTable, depth, scopeState)
            statements.add(statement)
            currentStatements++

            generateAnotherStatement = if (
                (statement.stat == ContextSpecificStat.BREAK && CNFG.preventCodeAfterBreak)
                || statement.stat == ContextSpecificStat.FALLTHROUGH
            ) {
                false
            } else {
                PRNG.evaluateProbability(CNFG.probabilityGenerateAnotherStatement)
            }
        }

        return this
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