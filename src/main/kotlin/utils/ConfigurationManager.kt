package wgslsmith.wgslgenerator.utils

internal object ConfigurationManager {
    const val maxStatementsInBody = 20
    const val maxStatementsInIfBody = 5
    const val maxStatementsInSwitchBody = 3
    const val maxExpressionRecursion = 10
    const val maxStatementRecursion = 3
    const val maxParentheses = 10
    const val maxIfElseBranches = 5
    const val maxSwitchCases = 10

    const val useSafeWrappers = false
    const val useExpressionParentheses = true
    const val useExcessParentheses = true
    
    const val ensureNoDuplicateSwitchCases = false
    const val ensureNoFallthroughLastSwitchCase = true

    const val probabilityOmitTypeFromDeclaration = 0.2
    const val probabilityParenthesesAroundIdentity = 0.5
    const val probabilityGenerateAnotherStatement = 0.9
    const val probabilityGenerateCompoundAssignment = 0.2
    const val probabilityAssignToAnonymous = 0.1
    const val probabilityAssignToNewSymbol = 0.6
    const val probabilityAssignLiteral = 0.3
    const val probabilityZeroLiteral = 0.01

    const val probabilityIfElseBranch = 0.3
    const val probabilityElseBranch = 0.6
    const val probabilitySwitchCase = 0.4
    const val probabilitySwitchDefaultBeforeLast = 0.1
}