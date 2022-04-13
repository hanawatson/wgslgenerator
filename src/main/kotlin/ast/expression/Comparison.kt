package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal enum class ComparisonForms(val comparator: String) {
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    MORE_THAN(">"),
    MORE_THAN_OR_EQUAL(">=");
}

internal object ComparisonGenerator {
    private val comparisonForms = ComparisonForms.values().asList()

    fun get(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int): Expression {
        val comparisonFormIndex = PseudoNumberGenerator.getRandomIntInRange(
            0, comparisonForms.size
        )
        val comparisonForm = comparisonForms[comparisonFormIndex]
        return ComparisonExpression(comparisonForm).generate(
            symbolTable,
            expressionType,
            depth
        )
    }
}

private class ComparisonExpression(private val comparisonForm: ComparisonForms) : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var argumentType: WGSLType
    override lateinit var expressionType: WGSLType

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int):
            ComparisonExpression {
        argumentType = PseudoNumberGenerator.getRandomWGSLType()
        lhs = ExpressionGenerator.getExpressionWithType(symbolTable, argumentType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithType(symbolTable, argumentType, depth + 1)
        this.expressionType = expressionType

        return this
    }

    override fun toString(): String {
        val lhsString =
            if (ConfigurationManager.useExpressionParentheses && lhs !is IdentityExpression) "($lhs)" else "$lhs"
        val rhsString =
            if (ConfigurationManager.useExpressionParentheses && rhs !is IdentityExpression) "($rhs)" else "$rhs"

        return "$lhsString ${comparisonForm.comparator} $rhsString"
    }
}