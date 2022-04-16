package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat
import wgslsmith.wgslgenerator.ast.statement.Statement
import wgslsmith.wgslgenerator.ast.statement.StatementGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class ScopeBody(private val scopeState: ScopeState) {
    private val statements: ArrayList<Statement> = ArrayList()

    fun getLastStatement(): Statement {
        return statements.last()
    }

    fun replaceLastStatement(statement: Statement) {
        statements.remove(statements.last())
        statements.add(statement)
    }

    fun generate(symbolTable: SymbolTable, depth: Int): ScopeBody {
        var currentStatements = 0
        var generateAnotherStatement = true
        val maxStatements = when (scopeState) {
            ScopeState.IF     -> ConfigurationManager.maxStatementsInIfBody
            ScopeState.SWITCH -> ConfigurationManager.maxStatementsInSwitchBody
            else              -> ConfigurationManager.maxStatementsInBody
        }

        while (generateAnotherStatement && currentStatements < maxStatements) {
            val statement = StatementGenerator.getStatement(symbolTable, depth, scopeState)
            statements.add(statement)

            generateAnotherStatement = if (statement.stat == ContextSpecificStat.BREAK || statement.stat ==
                ContextSpecificStat.FALLTHROUGH) {
                false
            } else {
                PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityGenerateAnotherStatement)
            }
            currentStatements++
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