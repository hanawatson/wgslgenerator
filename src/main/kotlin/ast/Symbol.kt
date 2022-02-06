package wgslsmith.wgslgenerator.ast

class Symbol(private val name: String, private val type: WGSLType) {
    fun getName(): String {
        return name
    }

    fun getType(): WGSLType {
        return type
    }
}