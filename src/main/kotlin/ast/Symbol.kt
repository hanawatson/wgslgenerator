package wgslsmith.wgslgenerator.ast

internal class Symbol(val name: String, val type: WGSLType) {

    override fun toString(): String {
        return name
    }
}