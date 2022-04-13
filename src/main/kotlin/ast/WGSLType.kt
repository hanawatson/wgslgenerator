package internalProgRep

enum class Type(val wgslType: String) {
    BOOL("bool"),
    FLOAT("f32"),
    INT("i32"),
    UNINT("u32");
}

interface WGSLType {
    val type: Type
}

class WGSLScalarType(override val type: Type) : WGSLType {
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