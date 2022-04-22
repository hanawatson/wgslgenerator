package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeBody
import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IfStatement(symbolTable: SymbolTable, override var stat: Stat, depth: Int) : Statement {
    private var ifCond: Expression
    private var ifBody: ScopeBody
    private var elseIfConds = ArrayList<Expression>()
    private var elseIfBodies = ArrayList<ScopeBody>()
    private var currentIfElseBranches = 0
    private var elseBody: ScopeBody? = null

    init {
        ifCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, WGSLScalarType(Type.BOOL), 0)
        ifBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)
        while (PRNG.evaluateProbability(CNFG.probabilityIfElseBranch)
            && currentIfElseBranches < CNFG.maxIfElseBranches) {
            val elseIfCond =
                ExpressionGenerator.getExpressionWithReturnType(symbolTable, WGSLScalarType(Type.BOOL), 0)
            val elseIfBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)
            elseIfConds.add(elseIfCond)
            elseIfBodies.add(elseIfBody)
            currentIfElseBranches++
        }
        if (PRNG.evaluateProbability(CNFG.probabilityElseBranch)) {
            elseBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        val ifLines = ArrayList<String>()
        ifLines.add("if ($ifCond) {")
        ifLines.addAll(ifBody.getTabbedLines())

        for (elseIfCond in elseIfConds) {
            val elseIfIndex = elseIfConds.indexOf(elseIfCond)
            ifLines.add("} else if ($elseIfCond) {")
            ifLines.addAll(elseIfBodies[elseIfIndex].getTabbedLines())
        }

        if (elseBody != null) {
            ifLines.add("} else {")
            ifLines.addAll(elseBody!!.getTabbedLines())
        }

        ifLines.add("}")
        return ifLines
    }
}