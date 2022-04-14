package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class BreakStatement : Statement() {
    override fun generate(symbolTable: SymbolTable, depth: Int): Statement {
        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("break;")
    }
}

internal class FallthroughStatement : Statement() {
    override fun generate(symbolTable: SymbolTable, depth: Int): Statement {
        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("fallthrough;")
    }
}

internal class IfStatement : Statement() {
    private lateinit var ifCond: Expression
    private lateinit var ifBody: ScopeBody
    private var elseIfConds = ArrayList<Expression>()
    private var elseIfBodies = ArrayList<ScopeBody>()
    private var currentIfElseBranches = 0
    private var elseBody: ScopeBody? = null

    override fun generate(symbolTable: SymbolTable, depth: Int): IfStatement {
        // can mat/vecs of bools be used here? must check
        ifCond = ExpressionGenerator.getExpressionWithReturnType(symbolTable, WGSLScalarType(Type.BOOL), 0)
        ifBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)

        while (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityIfElseBranch)
            && currentIfElseBranches < ConfigurationManager.maxIfElseBranches) {
            val elseIfCond =
                ExpressionGenerator.getExpressionWithReturnType(symbolTable, WGSLScalarType(Type.BOOL), 0)
            val elseIfBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)
            elseIfConds.add(elseIfCond)
            elseIfBodies.add(elseIfBody)
            currentIfElseBranches++
        }

        if (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityElseBranch)) {
            elseBody = ScopeBody(ScopeState.IF).generate(symbolTable.copy(), depth + 1)
        }

        return this
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

internal class SwitchStatement : Statement() {
    private lateinit var selector: Expression
    private lateinit var selectorType: WGSLType
    private var switchLiterals = ArrayList<Literal>()
    private var switchCases = ArrayList<Expression?>()
    private var switchBodies = ArrayList<ScopeBody>()
    private var currentSwitchCases = 0
    private var defaultGenerated = false

    override fun generate(symbolTable: SymbolTable, depth: Int): SwitchStatement {
        selectorType = WGSLScalarType(
            if (PseudoNumberGenerator.getRandomBool()) Type.INT else Type.UNINT
        )
        selector = ExpressionGenerator.getExpressionWithReturnType(symbolTable, selectorType, 0)

        while (currentSwitchCases < 1 ||
            (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilitySwitchCase)
                    && currentSwitchCases < ConfigurationManager.maxSwitchCases)) {

            val switchCase: Expression?
            if (!defaultGenerated && PseudoNumberGenerator.evaluateProbability(
                    ConfigurationManager.probabilitySwitchDefaultBeforeLast
                )) {
                switchCase = null
                defaultGenerated = true
            } else {
                var switchLiteral = LiteralGenerator.getLiteral(selectorType)
                while (ConfigurationManager.ensureNoDuplicateSwitchCases &&
                    switchLiterals.contains(switchLiteral)) {
                    switchLiteral = LiteralGenerator.getLiteral(selectorType)
                }
                switchLiterals.add(switchLiteral)
                switchCase = ExpressionGenerator.getLiteralAsExpression(switchLiteral)
            }
            val switchBody = ScopeBody(ScopeState.SWITCH).generate(symbolTable.copy(), depth + 1)

            switchCases.add(switchCase)
            switchBodies.add(switchBody)
            currentSwitchCases++
        }

        // make sure there is a default case if not generated already, and make sure the final statement is not a
        // fallthrough. substitute fallthrough for break if so to preserve supposed behaviour
        if (!defaultGenerated) {
            switchCases[currentSwitchCases - 1] = null
            defaultGenerated = true
        }
        if (ConfigurationManager.ensureNoFallthroughLastSwitchCase && switchBodies[currentSwitchCases - 1]
                .getLastStatement() is FallthroughStatement) {
            switchBodies[currentSwitchCases - 1].replaceLastStatement(BreakStatement())
        }

        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        val switchLines = ArrayList<String>()
        switchLines.add("switch ($selector) {")

        for (switchCase in switchCases) {
            val switchCaseLines = ArrayList<String>()
            val switchCaseIndex = switchCases.indexOf(switchCase)
            if (switchCase == null) {
                switchCaseLines.add("default: {")
            } else {
                switchCaseLines.add("case $switchCase: {")
            }
            switchCaseLines.addAll(switchBodies[switchCaseIndex].getTabbedLines())
            switchCaseLines.add("}")

            for (switchCaseLine in switchCaseLines) {
                switchLines.add("\t" + switchCaseLine)
            }
        }

        switchLines.add("}")
        return switchLines
    }
}