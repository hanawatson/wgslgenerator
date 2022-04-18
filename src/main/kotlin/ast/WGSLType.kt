package wgslsmith.wgslgenerator.ast

enum class Type {
    // ANY type used only as a stand-in for generation purposes!
    ANY,
    BOOL,
    FLOAT,
    INT,
    UNINT,
    VEC;
}

internal interface WGSLType {
    val type: Type

    // return true if abstractType is a type using e.g. ANY such that the (class property)
    // type is included in it (i.e. with abstractType as an abstracted/generalised definition)
    fun isRepresentedBy(abstractType: WGSLType): Boolean
}

internal class WGSLScalarType(override val type: Type) : WGSLType {
    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLScalarType) {
            return abstractType.type == Type.ANY
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLScalarType) {
            return type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return when (type) {
            Type.BOOL  -> "bool"
            Type.FLOAT -> "f32"
            Type.INT   -> "i32"
            Type.UNINT -> "u32"
            else       -> throw Exception(
                "Attempt to generate string representation of WGSLScalarType of unknown type $type!"
            )
        }
    }
}

internal class WGSLVectorType(val componentType: WGSLScalarType, val length: Int) : WGSLType {
    override val type = Type.VEC

    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLVectorType) {
            if (abstractType.componentType == WGSLScalarType(Type.ANY)) {
                return abstractType.length == 0 || abstractType.length == length
            }
            if (abstractType.length == 0) {
                return abstractType.componentType ==
                        WGSLScalarType(Type.ANY) || abstractType.componentType == componentType
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLVectorType) {
            return componentType == other.componentType && length == other.length
        }
        return false
    }

    override fun hashCode(): Int {
        var result = componentType.hashCode()
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "vec$length<$componentType>"
    }
}

internal val abstractWGSLScalarType = WGSLScalarType(Type.ANY)
internal val abstractWGSLVectorType = WGSLVectorType(abstractWGSLScalarType, 0)

internal val allTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLScalarType,
    abstractWGSLVectorType
)
internal val scalarTypes: ArrayList<WGSLType> = arrayListOf(
    WGSLScalarType(Type.BOOL),
    WGSLScalarType(Type.FLOAT),
    WGSLScalarType(Type.INT),
    WGSLScalarType(Type.UNINT)
)
internal val numericTypes: ArrayList<WGSLType> = arrayListOf(
    WGSLScalarType(Type.FLOAT),
    WGSLScalarType(Type.INT),
    WGSLScalarType(Type.UNINT)
)
internal val floatTypes: ArrayList<WGSLType> = arrayListOf(
    WGSLScalarType(Type.FLOAT)
)
internal val constructibleTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLVectorType
)