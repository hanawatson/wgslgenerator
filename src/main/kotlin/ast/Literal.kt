package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

object LiteralGenerator {
    fun getLiteral(type: WGSLType): Literal {
        val literalString = when (type.type) {
            Type.BOOL  -> "${PseudoNumberGenerator.getRandomBool()}"
            Type.FLOAT -> "${PseudoNumberGenerator.getRandomFloat()}f"
            Type.INT   -> "${PseudoNumberGenerator.getRandomIntInRange(Int.MIN_VALUE, Int.MAX_VALUE)}"
            Type.UNINT -> "${PseudoNumberGenerator.getRandomUnIntInRange(UInt.MIN_VALUE, UInt.MAX_VALUE)}u"
            // else       -> throw Exception("Attempt to generate literal of unknown type!")
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