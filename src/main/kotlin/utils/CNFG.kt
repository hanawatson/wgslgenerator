package wgslsmith.wgslgenerator.utils

// ConfigurationManager
internal object CNFG {
    const val maxStatementsInBody = 5 //20
    const val maxStatementsInIfBody = 5
    const val maxStatementsInSwitchBody = 3
    const val maxExpressionRecursion = 5 //10
    const val maxStatementRecursion = 3
    const val maxParentheses = 10
    const val maxIfElseBranches = 3 //5
    const val maxSwitchCases = 2 //10

    const val useExpressionParentheses = true
    const val useExcessParentheses = true

    const val preventCodeAfterBreak = true

    const val ensureNoDuplicateSwitchCases = false
    const val ensureNoFallthroughLastSwitchCase = true

    const val probabilityOmitTypeFromDeclaration = 0.2
    const val probabilityOmitTypeFromConstructible = 0.3
    const val probabilityParenthesesAroundIdentity = 0.5
    const val probabilityGenerateAnotherStatement = 0.9
    const val probabilityAssignToNewSymbol = 0.6

    // const val probabilityUseNumericSuffix = 0.5

    const val probabilityIfElseBranch = 0.3
    const val probabilityElseBranch = 0.6
    const val probabilitySwitchCase = 0.4
    const val probabilitySwitchDefaultBeforeLast = 0.1
}