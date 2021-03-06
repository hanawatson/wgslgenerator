package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.ast.scalarUnIntType
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.CNFG.nagaSafe
import wgslsmith.wgslgenerator.utils.CNFG.tintSafe
import wgslsmith.wgslgenerator.utils.PRNG
import java.lang.Float.toHexString

internal class IdentityLiteralExpression(override val returnType: WGSLType, override var expr: Expr) : Expression {
    private var useHex: Boolean = false
    private var useSuffix: Boolean = false
    private var literalSuffix: String = ""

    var literalValue: String
    private val constValues: ArrayList<*>

    override var numberOfParentheses = 0

    init {
        useSuffix = PRNG.eval(CNFG.useSuffixWithNumericLiteral)
        // enable and make mandatory u suffixes only due to lack of implementation in Tint and naga
        useSuffix = if (tintSafe || nagaSafe) returnType == scalarUnIntType else false
        useHex = PRNG.eval(CNFG.useHexadecimalNumericLiteral)

        literalValue = when (returnType.type) {
            Type.BOOL  -> {
                val bool = PRNG.getRandomBool()
                constValues = arrayListOf(bool)
                "$bool"
            }
            Type.FLOAT -> {
                literalSuffix = "f"
                val float = PRNG.getRandomPositiveFloat()
                constValues = arrayListOf(float)
                if (useHex) toHexString(float) else "$float"
            }
            Type.INT   -> {
                literalSuffix = "i"
                val int = PRNG.getRandomIntInRange(0, Int.MAX_VALUE)
                constValues = arrayListOf(int)
                if (useHex) "0x${int.toString(16)}" else "$int"
            }
            Type.UNINT -> {
                literalSuffix = "u"
                val unInt = PRNG.getRandomUnIntInRange(0u, UInt.MAX_VALUE)
                constValues = arrayListOf(unInt)
                if (useHex) "0x${unInt.toString(16)}" else "$unInt"
            }
            else       -> throw Exception("Attempt to generate literal of unknown type $returnType!")
        }
    }

    // return a specific int value in expression form
    constructor(value: Int) : this(scalarIntType, IdentityScalarExpr.LITERAL) {
        this.literalValue = if (useHex) "0x${value.toString(16)}" else "$value"
    }

    override fun toString(): String {
        if (!useSuffix) literalSuffix = ""
        return literalValue + literalSuffix
    }

    override fun getConstValue(): ArrayList<*> {
        return constValues
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is IdentityLiteralExpression) {
            return literalValue == other.literalValue && returnType == other.returnType
        }
        return false
    }

    override fun hashCode(): Int {
        var result = literalValue.hashCode()
        result = 31 * result + returnType.hashCode()
        return result
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            return arrayListOf(returnType)
        }
    }
}