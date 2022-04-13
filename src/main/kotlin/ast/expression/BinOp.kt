package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLTypeEnum
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator
import wgslsmith.wgslgenerator.utils.SafeWrapperManager

interface BinOpForms {
    val binOp: String
}

internal enum class BinOpArithmeticForms(override val binOp: String, val longOp: String) : BinOpForms {
    ADD("+", "add"),
    DIV("/", "div"),
    MINUS("-", "minus"),
    MOD("%", "mod"),
    MULT("*", "mult");
}

internal enum class BinOpBitForms(override val binOp: String) : BinOpForms {
    BIT_OR("|"),
    BIT_AND("&"),
    BIT_EXCLUSIVE_OR("^"),
    SHIFT_LEFT("<<"),
    SHIFT_RIGHT(">>"); // SHIFT_RIGHT is LOGICAL with type UNINT, ARITHMETIC with type INT
}

// when vectors are introduced maybe make another enum with only OR / AND? then pick from there if type vec<bool>
// actually we can add together ops because we are making arraylists. no duped enums!

internal enum class BinOpLogicalForms(override val binOp: String) : BinOpForms {
    SHORT_OR("||"),
    SHORT_AND("&&"),
    OR("|"),
    AND("&");
}

internal object BinOpGenerator {
    private val binOpArithmeticForms = ArrayList<BinOpForms>(BinOpArithmeticForms.values().asList())
    private val binOpBitForms = ArrayList<BinOpForms>(BinOpBitForms.values().asList())
    private val binOpLogicalForms = ArrayList<BinOpForms>(BinOpLogicalForms.values().asList())

    fun get(
        symbolTable: SymbolTable,
        expressionType: WGSLType,
        depth: Int,
        opType: ExpressionForms
    ): Expression {
        val binOpForms: ArrayList<BinOpForms> = when (opType) {
            ExpressionForms.BIN_OP_ARITHMETIC -> binOpArithmeticForms
            ExpressionForms.BIN_OP_BIT        -> binOpBitForms
            else                              -> binOpLogicalForms
        }

        val binOpFormIndex = PseudoNumberGenerator.getRandomIntInRange(
            0, binOpForms.size
        )
        val binOpForm = binOpForms[binOpFormIndex]
        return BinOpExpression(binOpForm).generate(
            symbolTable,
            expressionType,
            depth
        )
    }
}

private class BinOpExpression(private val binOpForm: BinOpForms) : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var rhsType: WGSLType
    override lateinit var expressionType: WGSLType

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int):
            BinOpExpression {
        lhs = ExpressionGenerator.getExpressionWithType(symbolTable, expressionType, depth + 1)
        rhsType =
            if (binOpForm == BinOpBitForms.SHIFT_LEFT || binOpForm == BinOpBitForms.SHIFT_RIGHT) {
                // if expressionType is vec, this must also be vec
                WGSLScalarType(WGSLTypeEnum.UNINT)
            } else {
                expressionType
            }
        rhs = ExpressionGenerator.getExpressionWithType(symbolTable, rhsType, depth + 1)
        this.expressionType = expressionType

        return this
    }

    override fun toString(): String {
        if (binOpForm is BinOpArithmeticForms && ConfigurationManager.useSafeWrappers) {
            val safeWrapper = SafeWrapperManager.getBinOpArithmeticSafeWrapper(expressionType, binOpForm)
            return "$safeWrapper($lhs, $rhs)"
        }

        val lhsString =
            if (ConfigurationManager.useExpressionParentheses && lhs !is IdentityExpression) "($lhs)" else "$lhs"
        val rhsString =
            if (ConfigurationManager.useExpressionParentheses && rhs !is IdentityExpression) "($rhs)" else "$rhs"

        return "$lhsString ${binOpForm.binOp} $rhsString"
    }
}