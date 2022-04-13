package wgslsmith.wgslgenerator.ast.expression

import internalProgRep.Type
import internalProgRep.WGSLScalarType
import internalProgRep.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator
import wgslsmith.wgslgenerator.utils.SafeWrapperManager

interface BinOpForms {
    val binOp: String
}

internal object BinOpGenerator {
    private val binOpArithmeticForms = ArrayList<BinOpForms>(BinOpArithmeticForms.values().asList())
    private val binOpBitForms = ArrayList<BinOpForms>(BinOpBitForms.values().asList())
    private val binOpLogicalForms = ArrayList<BinOpForms>(BinOpLogicalForms.values().asList())

    fun get(
        symbolTable: SymbolTable,
        expressionType: WGSLType,
        depth: Int,
        opType: ExprForms
    ): Expression {
        val binOpForms: ArrayList<BinOpForms> = when (opType) {
            ExprForms.BIN_OP_ARITHMETIC -> binOpArithmeticForms
            ExprForms.BIN_OP_BIT        -> binOpBitForms
            else                        -> binOpLogicalForms
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
    override lateinit var returnType: WGSLType

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int):
            BinOpExpression {
        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, expressionType, depth + 1)
        rhsType =
            if (binOpForm == BinOpBitForms.SHIFT_LEFT || binOpForm == BinOpBitForms.SHIFT_RIGHT) {
                // if expressionType is vec, this must also be vec
                WGSLScalarType(Type.UNINT)
            } else {
                expressionType
            }
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)
        this.returnType = expressionType

        return this
    }

    override fun toString(): String {
        if (binOpForm is BinOpArithmeticForms && ConfigurationManager.useSafeWrappers) {
            val safeWrapper = SafeWrapperManager.getBinOpArithmeticSafeWrapper(returnType, binOpForm)
            return "$safeWrapper($lhs, $rhs)"
        }

        val lhsString =
            if (ConfigurationManager.useExpressionParentheses && lhs !is IdentityExpression) "($lhs)" else "$lhs"
        val rhsString =
            if (ConfigurationManager.useExpressionParentheses && rhs !is IdentityExpression) "($rhs)" else "$rhs"

        return "$lhsString ${binOpForm.binOp} $rhsString"
    }
}