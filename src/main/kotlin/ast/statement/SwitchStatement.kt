package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.ScopeBody
import wgslsmith.wgslgenerator.ast.ScopeState
import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.expression.IdentityLiteralExpression
import wgslsmith.wgslgenerator.ast.expression.IdentityScalarExpr
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class SwitchStatement(symbolTable: SymbolTable, override var stat: Stat, depth: Int) : Statement {
    private var selectorType = PRNG.getRandomTypeFrom(arrayListOf(WGSLScalarType(Type.INT), WGSLScalarType(Type.UNINT)))
    private var selector = ExpressionGenerator.getExpressionWithReturnType(symbolTable, selectorType, 0)
    private var switchCases = ArrayList<IdentityLiteralExpression?>()
    private var switchBodies = ArrayList<ScopeBody>()
    private var currentSwitchCases = 0
    private var defaultGenerated = false

    init {
        while (currentSwitchCases < 1 ||
            (PRNG.eval(CNFG.generateSwitchCase) && currentSwitchCases < CNFG.maxSwitchCases)) {

            if (!defaultGenerated && PRNG.eval(CNFG.generateDefaultSwitchCaseBeforeLast)) {
                defaultGenerated = true
                switchCases.add(null)
            } else {
                var switchCase = IdentityLiteralExpression(selectorType, IdentityScalarExpr.LITERAL)
                while (CNFG.ensureNoDuplicateSwitchCases && switchCases.contains(switchCase)) {
                    switchCase = IdentityLiteralExpression(selectorType, IdentityScalarExpr.LITERAL)
                }
                switchCases.add(switchCase)
            }
            val switchBody = ScopeBody(ScopeState.SWITCH).generate(symbolTable.copy(), depth + 1)
            switchBodies.add(switchBody)
            currentSwitchCases++
        }
        if (!defaultGenerated) {
            switchCases[currentSwitchCases - 1] = null
            defaultGenerated = true
        }
        if (CNFG.preventFallthroughInLastSwitchCase && switchBodies[currentSwitchCases - 1]
                .getLastStatement().stat == ContextSpecificStat.FALLTHROUGH) {
            switchBodies[currentSwitchCases - 1].replaceLastStatement(
                ContextSpecificStatement(ContextSpecificStat.BREAK)
            )
        }
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