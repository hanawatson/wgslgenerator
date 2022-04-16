package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class SwitchStatement : Statement() {
    override lateinit var stat: Stat
    private lateinit var selector: Expression
    private lateinit var selectorType: WGSLType
    private var switchLiterals = ArrayList<Literal>()
    private var switchCases = ArrayList<Expression?>()
    private var switchBodies = ArrayList<ScopeBody>()
    private var currentSwitchCases = 0
    private var defaultGenerated = false

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): SwitchStatement {
        this.stat = stat

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
                .getLastStatement().stat == ContextSpecificStat.FALLTHROUGH) {
            switchBodies[currentSwitchCases - 1].replaceLastStatement(
                StatementGenerator.getContextSpecificStatement
                    (symbolTable, ContextSpecificStat.BREAK, depth)
            )
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