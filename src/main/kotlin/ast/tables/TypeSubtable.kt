package wgslsmith.wgslgenerator.ast.tables

import wgslsmith.wgslgenerator.ast.Symbol

class TypeSubtable {
    private var writeableIndex: Int = 0 // var or val. man or muppet? this is the first index that is NOT readonly
    private var nextIndex: Int = 0 // the next index to insert at
    private val symbols: HashMap<Int, Symbol> = hashMapOf() // insert a symbol at NextIndex.

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
}