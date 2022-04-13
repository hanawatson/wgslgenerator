package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType

class TypeSubtable(val type: WGSLType) {
    private var writeableIndex: Int = 0 // var or val. man or muppet? this is the first index that is NOT readonly
    private var nextIndex: Int = 0 // the next index to insert at
    private var symbols: HashMap<Int, Symbol> = hashMapOf() // insert a symbol at NextIndex.

    fun isEmpty(ofWriteable: Boolean): Boolean {
        if (ofWriteable) {
            return writeableIndex == nextIndex
        }
        return nextIndex == 0
    }

    fun addSymbol(symbol: Symbol) {
        symbols[nextIndex] = symbol
        nextIndex++
    }

    fun addNonWriteableSymbol(symbol: Symbol) {
        if (nextIndex != writeableIndex) {
            throw Exception("Non-writeable symbol illegally declared after start of program generation!")
        }
        addSymbol(symbol)
        writeableIndex = nextIndex
    }

    fun getWriteableIndex(): Int {
        return writeableIndex
    }

    fun getNextIndex(): Int {
        return nextIndex
    }

    fun getSymbolAtIndex(index: Int): Symbol? {
        return symbols[index]
    }

    internal fun copy(): TypeSubtable {
        val typeSubtable = TypeSubtable(type)

        typeSubtable.writeableIndex = this.writeableIndex
        typeSubtable.nextIndex = this.nextIndex
        typeSubtable.symbols = HashMap(this.symbols)

        return typeSubtable
    }
}