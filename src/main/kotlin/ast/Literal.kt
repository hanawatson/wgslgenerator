package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

object LiteralGenerator {
    fun getLiteral(type: WGSLType): Literal {
        if (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityZeroLiteral)) {
            return Literal("${type.type.wgslType}()", type)
        }
        val literalString = when (type.type) {
            WGSLTypeEnum.BOOL  -> "${PseudoNumberGenerator.getRandomBool()}"
            WGSLTypeEnum.FLOAT -> "${PseudoNumberGenerator.getRandomFloat()}f"
            WGSLTypeEnum.INT   -> "${PseudoNumberGenerator.getRandomIntInRange(Int.MIN_VALUE, Int.MAX_VALUE)}"
            WGSLTypeEnum.UNINT -> "${PseudoNumberGenerator.getRandomUnIntInRange(UInt.MIN_VALUE, UInt.MAX_VALUE)}u"
            // else               -> throw Exception("Unknown internal expression type handled!")
        }

        return Literal(literalString, type)
    }
}

class Literal(private val literalString: String, private val type: WGSLType) {
    fun getType(): WGSLType {
        return type
    }

    override fun toString(): String {
        return literalString
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Literal) {
            return literalString == other.literalString && type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        var result = literalString.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}