package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityLiteralExpression : Expression() {
    private lateinit var literalValue: String
    private var literalSuffix: String = ""

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): IdentityLiteralExpression {
        this.returnType = returnType
        this.expr = expr

        literalValue = when (returnType) {
            WGSLScalarType(Type.BOOL)  -> "${PRNG.getRandomBool()}"
            WGSLScalarType(Type.FLOAT) -> "${PRNG.getRandomFloat()}"
            WGSLScalarType(Type.INT)   -> "${PRNG.getRandomIntInRange(Int.MIN_VALUE, Int.MAX_VALUE)}"
            WGSLScalarType(Type.UNINT) -> "${PRNG.getRandomUnIntInRange(UInt.MIN_VALUE, UInt.MAX_VALUE)}"
            else                       -> throw Exception("Attempt to generate literal of unknown type $returnType!")
        }

        // temporarily commented due to lack of implementation of AbstractInt in Tint and naga
        // u suffix appended above for now - i not supported in Tint or naga, f not supported in naga
        // see https://github.com/gfx-rs/naga/issues/1843
        /* // append optional suffix if appropriate
        if (PRNG.evaluateProbability(CNFG.probabilityUseNumericSuffix)) {
            literalSuffix = when (type.type) {
                Type.FLOAT -> "f"
                Type.INT   -> "i"
                Type.UNINT -> "u"
                else       -> ""
            }
        }*/

        if (returnType.type == Type.UNINT) {
            literalSuffix = "u"
        }

        return this
    }

    override fun toString(): String {
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