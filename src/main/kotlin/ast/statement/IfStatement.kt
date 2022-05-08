package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeBody
import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.scalarBoolType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IfStatement(
    symbolTable: SymbolTable, override var stat: Stat, depth: Int, inLoop: Boolean
) : Statement {
    private val ifCond: Expression
    private val ifBody: ScopeBody
    private val elseIfConds = ArrayList<Expression>()
    private val elseIfBodies = ArrayList<ScopeBody>()
    private var currentIfElseBranches = 0
    private var elseBody: ScopeBody? = null

    init {
        var condType = PRNG.getRandomTypeFrom(usedTypes(stat))
        ifCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, condType, 0)
        ifBody = ScopeBody(symbolTable.copy(), ScopeState.IF, depth + 1, inLoop)
        while (PRNG.eval(CNFG.generateIfElseBranch) && currentIfElseBranches < CNFG.maxIfElseBranches) {
            condType = PRNG.getRandomTypeFrom(usedTypes(stat))
            val elseIfCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, condType, 0)
            val elseIfBody = ScopeBody(symbolTable.copy(), ScopeState.IF, depth + 1, inLoop)
            elseIfConds.add(elseIfCond)
            elseIfBodies.add(elseIfBody)
            currentIfElseBranches++
        }
        if (PRNG.eval(CNFG.generateElseBranch)) {
            elseBody = ScopeBody(symbolTable.copy(), ScopeState.IF, depth + 1, inLoop)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        val ifLines = ArrayList<String>()
        ifLines.add("if $ifCond {")
        ifLines.addAll(ifBody.getTabbedLines())

        for (elseIfCond in elseIfConds) {
            val elseIfIndex = elseIfConds.indexOf(elseIfCond)
            ifLines.add("} else if $elseIfCond {")
            ifLines.addAll(elseIfBodies[elseIfIndex].getTabbedLines())
        }

        if (elseBody != null) {
            ifLines.add("} else {")
            ifLines.addAll(elseBody!!.getTabbedLines())
        }

        ifLines.add("}")
        return ifLines
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return arrayListOf(scalarBoolType)
        }
    }
}