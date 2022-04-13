package wgslsmith.wgslgenerator.ast

enum class WGSLTypeEnum(val wgslType: String) {
    BOOL("bool"),
    FLOAT("f32"),
    INT("i32"),
    UNINT("u32");
}

interface WGSLType {
    val type: WGSLTypeEnum
}

class WGSLScalarType(override val type: WGSLTypeEnum) : WGSLType {
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