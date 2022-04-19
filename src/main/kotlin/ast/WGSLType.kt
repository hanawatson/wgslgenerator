package wgslsmith.wgslgenerator.ast

enum class Type {
    // ANY type used only as a stand-in for generation purposes!
    ANY,

    BOOL,
    FLOAT,
    INT,
    UNINT,

    MAT,
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
            return abstractType.type == Type.ANY || abstractType.type == type
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
            val matchesComponentType = abstractType.componentType == abstractWGSLScalarType
                    || abstractType.componentType == componentType
            val matchesLength = abstractType.length == 0 || abstractType.length == length

            return matchesComponentType && matchesLength
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

internal class WGSLMatrixType(val componentType: WGSLScalarType, val width: Int, val length: Int) : WGSLType {
    override val type = Type.MAT

    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLMatrixType) {
            val matchesComponentType = abstractType.componentType == abstractWGSLScalarType
                    || abstractType.componentType == componentType
            val matchesWidth = abstractType.width == 0 || abstractType.width == width
            val matchesLength = abstractType.length == 0 || abstractType.length == length

            return matchesComponentType && matchesWidth && matchesLength
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLMatrixType) {
            return componentType == other.componentType && width == other.width && length == other.length
        }
        return false
    }

    override fun hashCode(): Int {
        var result = componentType.hashCode()
        result = 31 * result + width
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "mat${width}x$length<$componentType>"
    }
}

internal val scalarBoolType = WGSLScalarType(Type.BOOL)
internal val scalarFloatType = WGSLScalarType(Type.FLOAT)
internal val scalarIntType = WGSLScalarType(Type.INT)
internal val scalarUnIntType = WGSLScalarType(Type.UNINT)

internal val vectorBoolType = WGSLVectorType(scalarBoolType, 0)
internal val vectorFloatType = WGSLVectorType(scalarFloatType, 0)
internal val vectorIntType = WGSLVectorType(scalarIntType, 0)
internal val vectorUnIntType = WGSLVectorType(scalarUnIntType, 0)

internal val vector2FloatType = WGSLVectorType(scalarFloatType, 2)
internal val vector3FloatType = WGSLVectorType(scalarFloatType, 3)
internal val vector4FloatType = WGSLVectorType(scalarFloatType, 4)

internal val abstractWGSLScalarType = WGSLScalarType(Type.ANY)
internal val abstractWGSLVectorType = WGSLVectorType(abstractWGSLScalarType, 0)
internal val abstractWGSLMatrixType = WGSLMatrixType(abstractWGSLScalarType, 0, 0)

internal val allTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLScalarType,
    abstractWGSLVectorType,
    abstractWGSLMatrixType
)
internal val scalarTypes: ArrayList<WGSLType> = arrayListOf(
    scalarBoolType,
    scalarFloatType,
    scalarIntType,
    scalarUnIntType
)
internal val numericScalarTypes: ArrayList<WGSLType> = arrayListOf(
    scalarFloatType,
    scalarIntType,
    scalarUnIntType
)
internal val numericVectorTypes: ArrayList<WGSLType> = arrayListOf(
    vectorFloatType,
    vectorIntType,
    vectorUnIntType
)
internal val numericTypes: ArrayList<WGSLType> = ArrayList(numericScalarTypes + numericVectorTypes)
internal val compositeTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLVectorType,
    abstractWGSLMatrixType
)
internal val matrixComponentTypes: ArrayList<WGSLType> = arrayListOf(
    scalarFloatType
)
internal val matrixColumnTypes: ArrayList<WGSLType> = arrayListOf(
    vectorFloatType
)