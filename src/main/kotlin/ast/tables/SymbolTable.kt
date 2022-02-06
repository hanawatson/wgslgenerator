package wgslsmith.wgslgenerator.ast.tables

import wgslsmith.wgslgenerator.ast.PseudoSelector
import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType

class SymbolTable {
    // holds a bunch of TypeSubtables.
    private val intSubtable: TypeSubtable = TypeSubtable()

    fun addSymbol(symbol: Symbol) {
        when (symbol.getType()) {
            WGSLType.INT -> intSubtable.addSymbol(symbol)
        }
    }

    fun addNonWriteableSymbol(symbol: Symbol) {
        when (symbol.getType()) {
            WGSLType.INT -> intSubtable.addNonWriteableSymbol(symbol)
        }
    }

    fun getRandomSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, false)
    }

    fun getRandomWriteableSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, true)
    }

    private fun getRandomSymbolFrom(type: WGSLType, mustBeWriteable: Boolean): Symbol {
        // inclusive
        val startIndex: Int
        // exclusive
        val endIndex: Int
        val subtable: TypeSubtable

        when (type) {
            WGSLType.INT -> subtable = intSubtable
        }

        startIndex = if (mustBeWriteable) {
            subtable.getWriteableIndex()
        } else {
            0
        }
        endIndex = subtable.getNextIndex()

        // get random index - a result of startIndex-1 means a blank symbol (signalling a new one) is returned
        val randomIndex = PseudoSelector.getRandomInRange(startIndex - 1, endIndex)
        if (randomIndex == startIndex - 1) {
            return Symbol("", type)
        } else if (subtable.getSymbolAtIndex(randomIndex) != null) {
            return subtable.getSymbolAtIndex(randomIndex)!!
        }

        throw Exception("Out-of-bounds symbol index generated!")
    }
}