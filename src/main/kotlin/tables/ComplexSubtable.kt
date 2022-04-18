package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType

internal abstract class ComplexSubtable : Subtable {
    abstract fun getSubtable(type: WGSLType): Subtable
    abstract fun getInnerType(type: WGSLType): WGSLType

    override fun isEmpty(type: WGSLType, ofWriteable: Boolean): Boolean {
        val innerType = getInnerType(type)
        return getSubtable(type).isEmpty(innerType, ofWriteable)
    }

    override fun addSymbol(type: WGSLType, symbol: Symbol, writeable: Boolean) {
        val innerType = getInnerType(type)
        getSubtable(type).addSymbol(innerType, symbol, writeable)
    }

    override fun getSymbolAtIndex(type: WGSLType, index: Int): Symbol? {
        val innerType = getInnerType(type)
        return getSubtable(type).getSymbolAtIndex(innerType, index)
    }

    override fun getNextIndexOf(type: WGSLType): Int {
        val innerType = getInnerType(type)
        return getSubtable(type).getNextIndexOf(innerType)
    }

    override fun getWriteableIndexOf(type: WGSLType): Int {
        val innerType = getInnerType(type)
        return getSubtable(type).getWriteableIndexOf(innerType)
    }
}