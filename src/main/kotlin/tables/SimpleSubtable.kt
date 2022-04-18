package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType

internal class SimpleSubtable : Subtable {
    private var writeableIndex: Int = 0 // the index of the first writeable symbol e.g. not read-only
    private var nextIndex: Int = 0 // the next index to insert at
    private var symbols: HashMap<Int, Symbol> = hashMapOf()

    override fun isEmpty(type: WGSLType, ofWriteable: Boolean): Boolean {
        if (ofWriteable) {
            return writeableIndex == nextIndex
        }
        return nextIndex == 0
    }

    override fun addSymbol(type: WGSLType, symbol: Symbol, writeable: Boolean) {
        if (writeable) {
            // writeableIndex is unchanged, indicating where the first writeable symbol has been placed
            symbols[nextIndex] = symbol
            nextIndex++
        } else {
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
    }

    override fun getSymbolAtIndex(type: WGSLType, index: Int): Symbol? {
        return symbols[index]
    }

    override fun getNextIndexOf(type: WGSLType): Int {
        return nextIndex
    }

    override fun getWriteableIndexOf(type: WGSLType): Int {
        return writeableIndex
    }

    override fun copy(): SimpleSubtable {
        val simpleSubtable = SimpleSubtable()

        simpleSubtable.writeableIndex = this.writeableIndex
        simpleSubtable.nextIndex = this.nextIndex
        simpleSubtable.symbols = HashMap(this.symbols)

        return simpleSubtable
    }
}