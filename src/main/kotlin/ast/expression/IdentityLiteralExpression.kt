package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG
import java.lang.Float.toHexString

internal class IdentityLiteralExpression(override val returnType: WGSLType, override var expr: Expr) : Expression {
    private var useHex: Boolean = false
    private var useSuffix: Boolean = false
    private var literalSuffix: String = ""

    var literalValue: String

    override var numberOfParentheses = 0

    init {
        // temporarily commented due to lack of implementation of AbstractInt in Tint and naga
        // u suffix appended above for now - i not supported in Tint or naga, f not supported in naga
        // see https://github.com/gfx-rs/naga/issues/1843
        // useSuffix = PRNG.evaluateProbability(CNFG.probabilityUseLiteralSuffix)

        useSuffix = returnType.type == Type.UNINT
        useHex = PRNG.evaluateProbability(CNFG.probabilityUseHexLiteral)

        literalValue = when (returnType.type) {
            Type.BOOL  -> "${PRNG.getRandomBool()}"
            Type.FLOAT -> {
                literalSuffix = "f"
                val float = PRNG.getRandomPositiveFloat()
                if (useHex) toHexString(float) else "$float"
            }
            Type.INT   -> {
                literalSuffix = "i"
                val int = PRNG.getRandomIntInRange(0, Int.MAX_VALUE)
                if (useHex) "0x${int.toString(16)}" else "$int"
            }
            Type.UNINT -> {
                literalSuffix = "u"
                val unInt = PRNG.getRandomUnIntInRange(0u, UInt.MAX_VALUE)
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
}