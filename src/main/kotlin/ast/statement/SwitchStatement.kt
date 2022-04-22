package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.expression.IdentityLiteralExpression
import wgslsmith.wgslgenerator.ast.expression.IdentityScalarExpr
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class SwitchStatement : Statement {
    override lateinit var stat: Stat
    private lateinit var selector: Expression
    private lateinit var selectorType: WGSLType
    private var switchCases = ArrayList<IdentityLiteralExpression?>()
    private var switchBodies = ArrayList<ScopeBody>()
    private var currentSwitchCases = 0
    private var defaultGenerated = false

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): SwitchStatement {
        this.stat = stat

        selectorType = PRNG.getRandomTypeFrom(arrayListOf(WGSLScalarType(Type.INT), WGSLScalarType(Type.UNINT)))
        selector = ExpressionGenerator.getExpressionWithReturnType(symbolTable, selectorType, 0)

        while (currentSwitchCases < 1 ||
            (PRNG.evaluateProbability(CNFG.probabilitySwitchCase)
                    && currentSwitchCases < CNFG.maxSwitchCases)) {

            if (!defaultGenerated && PRNG.evaluateProbability(
                    CNFG.probabilitySwitchDefaultBeforeLast
                )) {
                defaultGenerated = true
                switchCases.add(null)
            } else {
                var switchCase = IdentityLiteralExpression().generate(
                    symbolTable, selectorType, IdentityScalarExpr.LITERAL, depth
                )
                while (CNFG.ensureNoDuplicateSwitchCases && switchCases.contains(switchCase)) {
                    switchCase = IdentityLiteralExpression().generate(
                        symbolTable, selectorType, IdentityScalarExpr.LITERAL, depth
                    )
                }
                switchCases.add(switchCase)
            }
            val switchBody = ScopeBody(ScopeState.SWITCH).generate(symbolTable.copy(), depth + 1)
            switchBodies.add(switchBody)
            currentSwitchCases++
        }

        // make sure there is a default case if not generated already, and make sure the final statement is not a
        // fallthrough. substitute fallthrough for break if so to preserve supposed behaviour
        if (!defaultGenerated) {
            switchCases[currentSwitchCases - 1] = null
            defaultGenerated = true
        }
        if (CNFG.ensureNoFallthroughLastSwitchCase && switchBodies[currentSwitchCases - 1]
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