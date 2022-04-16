package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.utils.PRNG

object LiteralGenerator {
    fun getLiteral(type: WGSLType): Literal {
        val literalString = when (type) {
            WGSLScalarType(Type.BOOL)  -> "${PRNG.getRandomBool()}"
            WGSLScalarType(Type.FLOAT) -> "${PRNG.getRandomFloat()}"
            WGSLScalarType(Type.INT)   -> "${PRNG.getRandomIntInRange(Int.MIN_VALUE, Int.MAX_VALUE)}"
            WGSLScalarType(Type.UNINT) -> "${PRNG.getRandomUnIntInRange(UInt.MIN_VALUE, UInt.MAX_VALUE)}u"
            else                       -> throw Exception("Attempt to generate literal of unknown type $type!")
        }

        // temporarily commented due to lack of implementation of AbstractInt in Tint and naga
        // u suffix appended above for now - i not supported in Tint or naga, f not supported in naga
        /* // append optional suffix if appropriate
        if (PRNG.evaluateProbability(CNFG.probabilityUseNumericSuffix)) {
            literalString += when (type.type) {
                Type.FLOAT -> "f"
                Type.INT   -> "i"
                Type.UNINT -> "u"
                else       -> ""
            }
        }*/

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