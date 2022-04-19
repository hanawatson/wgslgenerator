package wgslsmith.wgslgenerator.utils

// ConfigurationManager
internal object CNFG {
    const val maxStatementsInBody = 5
    const val maxStatementsInIfBody = 5
    const val maxStatementsInSwitchBody = 3
    const val maxExpressionRecursion = 5
    const val maxStatementRecursion = 3
    const val maxParentheses = 10
    const val maxIfElseBranches = 5
    const val maxSwitchCases = 10

    const val useNecessaryExpressionParentheses = true // enables necessary parentheses (e.g. will error without use)
    const val useUsefulExpressionParentheses = true // enables "safe" parentheses (e.g. will reduce ambiguities)
    const val useExcessParentheses = true

    const val preventCodeAfterBreak = true

    const val ensureNoDuplicateSwitchCases = false
    const val ensureNoFallthroughLastSwitchCase = true
    const val ensureSubscriptAccessInBounds = true

    const val probabilityOmitTypeFromDeclaration = 0.2

    // temporarily zeroed due to lack of implementation in naga
    const val probabilityOmitTypeFromConstructible = 0.0
    const val probabilityParenthesesAroundExpression = 0.7
    const val probabilityGenerateAnotherStatement = 0.9
    const val probabilityAssignToNewSymbol = 0.6

    // const val probabilityUseNumericSuffix = 0.5
    const val probabilityGenerateVectorWithSingleValue = 0.2
    const val probabilityReplaceVectorBinaryOperandWithScalar = 0.3
    const val probabilityGenerateSubscriptAccessInBounds = 0.5

    const val probabilityIfElseBranch = 0.3
    const val probabilityElseBranch = 0.6
    const val probabilitySwitchCase = 0.4
    const val probabilitySwitchDefaultBeforeLast = 0.1
}