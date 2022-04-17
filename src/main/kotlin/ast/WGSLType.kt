package wgslsmith.wgslgenerator.ast

enum class Type(val wgslType: String) {
    // ANY type used only as a stand-in for generation purposes!
    ANY(""),
    BOOL("bool"),
    FLOAT("f32"),
    INT("i32"),
    UNINT("u32");
}

internal interface WGSLType {
    val type: Type
}

internal class WGSLScalarType(override val type: Type) : WGSLType {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLScalarType) {
            return type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}

internal val allTypes: ArrayList<WGSLType> = arrayListOf(WGSLScalarType(Type.ANY))
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