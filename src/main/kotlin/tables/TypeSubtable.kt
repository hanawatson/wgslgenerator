package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType

internal class TypeSubtable(val type: WGSLType) {
    private var writeableIndex: Int = 0 // the index of the first writeable symbol e.g. not read-only
    private var nextIndex: Int = 0 // the next index to insert at
    private var symbols: HashMap<Int, Symbol> = hashMapOf()

    fun isEmpty(ofWriteable: Boolean): Boolean {
        if (ofWriteable) {
            return writeableIndex == nextIndex
        }
        return nextIndex == 0
    }

    fun addWriteableSymbol(symbol: Symbol) {
        // writeableIndex is unchanged, indicating where the first writeable symbol has been placed
        symbols[nextIndex] = symbol
        nextIndex++
    }

    fun addNonWriteableSymbol(symbol: Symbol) {
        if (nextIndex != writeableIndex) {
            // move the first writeable symbol to a new index and replace it with read-only symbol
            // will not affect likelihood of being selected since this is independent of position/index
            val symbolToMove = symbols[writeableIndex]!!
            symbols[writeableIndex] = symbol
            symbols[nextIndex] = symbolToMove
        } else {
            symbols[nextIndex] = symbol
        }
        nextIndex++
        writeableIndex++
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