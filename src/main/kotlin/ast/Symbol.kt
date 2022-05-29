package wgslsmith.wgslgenerator.ast

internal open class Symbol(open val name: String, open val type: WGSLType) {
    override fun toString(): String {
        return name
    }

    fun getConstValue(): ArrayList<*> {
        if (this is ConstSymbol) {
            return this.value
        } else {
            throw Exception("Attempt to retrieve const value of non-const Symbol $this!")
        }
    }
}

internal class ConstSymbol(override val name: String, override val type: WGSLType, val value: ArrayList<*>) :
    Symbol(name, type)