package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG
import java.lang.Float.toHexString

internal class IdentityLiteralExpression : Expression() {
    private var useHex: Boolean = false
    private var useSuffix: Boolean = false
    private lateinit var literalValue: String
    private var literalSuffix: String = ""

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = 0

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): IdentityLiteralExpression {
        this.returnType = returnType
        this.expr = expr

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

        return this
    }

    fun generateIntLiteralInRange(symbolTable: SymbolTable, minValue: Int, maxValue: Int): IdentityLiteralExpression {
        this.generate(symbolTable, scalarIntType, IdentityScalarExpr.LITERAL, 0)
        val int = PRNG.getRandomIntInRange(minValue, maxValue)
        literalValue = if (useHex) "0x${int.toString(16)}" else "$int"

        return this
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