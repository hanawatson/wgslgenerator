package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator
import wgslsmith.wgslgenerator.utils.SafeWrapperManager

interface UnOpForms {
    val unOp: String
}

internal enum class UnOpArithmeticForms(override val unOp: String, val longOp: String) : UnOpForms {
    NEGATE("-", "negate");
}

internal enum class UnOpBitForms(override val unOp: String) : UnOpForms {
    COMPLEMENT("~");
}

internal enum class UnOpLogicalForms(override val unOp: String) : UnOpForms {
    NOT("!");
}

internal object UnOpGenerator {
    private val unOpArithmeticForms = ArrayList<UnOpForms>(UnOpArithmeticForms.values().asList())
    private val unOpBitForms = ArrayList<UnOpForms>(UnOpBitForms.values().asList())
    private val unOpLogicalForms = ArrayList<UnOpForms>(UnOpLogicalForms.values().asList())

    fun get(
        symbolTable: SymbolTable,
        expressionType: WGSLType,
        depth: Int,
        opType: ExpressionForms
    ): Expression {
        val unOpForms: ArrayList<UnOpForms> = when (opType) {
            ExpressionForms.UNARY_OP_ARITHMETIC -> unOpArithmeticForms
            ExpressionForms.UNARY_OP_BIT        -> unOpBitForms
            else                                -> unOpLogicalForms
        }

        val unOpFormIndex = PseudoNumberGenerator.getRandomIntInRange(
            0, unOpForms.size
        )
        val unOpForm = unOpForms[unOpFormIndex]
        return UnOpExpression(unOpForm).generate(
            symbolTable,
            expressionType,
            depth
        )
    }
}

private class UnOpExpression(private val unOpForm: UnOpForms) : Expression() {
    private lateinit var inner: Expression
    override lateinit var expressionType: WGSLType

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int):
            UnOpExpression {
        inner = ExpressionGenerator.getExpressionWithType(symbolTable, expressionType, depth + 1)
        this.expressionType = expressionType

        return this
    }

    override fun toString(): String {
        if (unOpForm is UnOpArithmeticForms && ConfigurationManager.useSafeWrappers) {
            val safeWrapper = SafeWrapperManager.getUnOpArithmeticSafeWrapper(expressionType, unOpForm)
            return "$safeWrapper($inner)"
        }

        val innerString =
            if (ConfigurationManager.useExpressionParentheses && inner !is IdentityExpression) "($inner)" else "$inner"

        return "${unOpForm.unOp}$innerString"
    }
}