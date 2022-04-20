package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.PRNG

internal interface Subtable {
    fun isEmpty(type: WGSLType, ofWriteable: Boolean): Boolean
    fun addSymbol(type: WGSLType, symbol: Symbol, writeable: Boolean)
    fun getSymbolAtIndex(type: WGSLType, index: Int): Symbol?

    fun getNextIndexOf(type: WGSLType): Int
    fun getWriteableIndexOf(type: WGSLType): Int

    fun copy(): Subtable
}

internal class SymbolTable {
    private var scalarSubtable = ScalarSubtable()
    private var vectorSubtable = VectorSubtable()
    private var matrixSubtable = MatrixSubtable()

    private var newVarLabelIndex: Int = 0

    private fun getSubtable(type: WGSLType): Subtable {
        return when (type) {
            is WGSLScalarType -> scalarSubtable
            is WGSLVectorType -> vectorSubtable
            is WGSLMatrixType -> matrixSubtable
            else              -> throw Exception("Attempt to access symbol subtable of unknown type!")
        }
    }

    fun hasWriteableOf(type: WGSLType): Boolean {
        return !getSubtable(type).isEmpty(type, ofWriteable = true)
    }

    private fun getNextNewSymbolName(): String {
        return "var$newVarLabelIndex"
    }

    fun declareNewWriteableSymbol(type: WGSLType): Symbol {
        return addSymbol(type, writeable = true)
    }

    fun declareNewNonWriteableSymbol(type: WGSLType): Symbol {
        return addSymbol(type, writeable = false)
    }

    private fun addSymbol(type: WGSLType, writeable: Boolean): Symbol {
        val symbol = Symbol(getNextNewSymbolName(), type)
        getSubtable(symbol.type).addSymbol(symbol.type, symbol, writeable)
        newVarLabelIndex++
        return symbol
    }

    fun getRandomSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, mustBeWriteable = false)
    }

    fun getRandomWriteableSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, mustBeWriteable = true)
    }

    private fun getRandomSymbolFrom(type: WGSLType, mustBeWriteable: Boolean): Symbol {
        val subtable = getSubtable(type)

        // inclusive
        val startIndex = if (mustBeWriteable) {
            subtable.getWriteableIndexOf(type)
        } else {
            0
        }
        // exclusive
        val endIndex = subtable.getNextIndexOf(type)

        val randomIndex = PRNG.getRandomIntInRange(startIndex, endIndex)
        if (subtable.getSymbolAtIndex(type, randomIndex) != null) {
            return subtable.getSymbolAtIndex(type, randomIndex)!!
        }

        throw Exception("Out-of-bounds symbol index generated!")
    }

    internal fun copy(): SymbolTable {
        val symbolTable = SymbolTable()

        symbolTable.newVarLabelIndex = this.newVarLabelIndex

        symbolTable.scalarSubtable = this.scalarSubtable.copy()
        symbolTable.vectorSubtable = this.vectorSubtable.copy()
        symbolTable.matrixSubtable = this.matrixSubtable.copy()

        return symbolTable
    }
}