package wgslsmith.wgslgenerator.ast

class Symbol(val name: String, val type: WGSLType) {

    override fun toString(): String {
        return name
    }
}