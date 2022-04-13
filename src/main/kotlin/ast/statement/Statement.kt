package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

private interface StatementForms

// Holds statements that can only be used in certain contexts e.g. fallthrough in a switch case
private enum class ContextSpecificStatementForms : StatementForms {
    BREAK,
    FALLTHROUGH;
}

private enum class StandardStatementForms : StatementForms {
    ASSIGN,
    IF,
    SWITCH;
}

object StatementGenerator {
    private val allStatementForms = ArrayList<StatementForms>(StandardStatementForms.values().asList())

    fun getStatement(symbolTable: SymbolTable, depth: Int, scopeState: ScopeState): Statement {
        var statementForms = ArrayList<StatementForms>()
        statementForms.addAll(allStatementForms)

        if (scopeState == ScopeState.SWITCH) {
            statementForms.add(ContextSpecificStatementForms.BREAK)
            statementForms.add(ContextSpecificStatementForms.FALLTHROUGH)
        }

        if (depth >= ConfigurationManager.maxStatementRecursion) {
            statementForms = arrayListOf(StandardStatementForms.ASSIGN)
        }

        val statementFormIndex = PseudoNumberGenerator.getRandomIntInRange(0, statementForms.size)
        return when (statementForms[statementFormIndex]) {
            ContextSpecificStatementForms.BREAK       -> BreakStatement()
            ContextSpecificStatementForms.FALLTHROUGH -> FallthroughStatement()
            StandardStatementForms.ASSIGN             -> AssignmentStatement().generate(symbolTable, depth)
            StandardStatementForms.IF                 -> IfStatement().generate(symbolTable, depth)
            StandardStatementForms.SWITCH             -> SwitchStatement().generate(symbolTable, depth)
            else                                      -> throw Exception("Unknown internal statement type handled!")
        }
    }
}

abstract class Statement {
    abstract fun generate(symbolTable: SymbolTable, depth: Int): Statement
    abstract fun getTabbedLines(): ArrayList<String>
}