package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.CNFG
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

    fun declareNewSymbol(type: WGSLType): Symbol {
        val newSymbol = Symbol(getNextNewSymbolName(), type)
        addSymbol(newSymbol, writeable = true)
        newVarLabelIndex++
        return newSymbol
    }

    private fun addSymbol(symbol: Symbol, writeable: Boolean) {
        getSubtable(symbol.type).addSymbol(symbol.type, symbol, writeable)
    }

    fun getRandomSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, mustBeWriteable = false, mustAlreadyExist = false)
    }

    fun getRandomWriteableSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, mustBeWriteable = true, mustAlreadyExist = false)
    }

    fun getRandomWriteableExistingSymbol(type: WGSLType): Symbol {
        return getRandomSymbolFrom(type, mustBeWriteable = true, mustAlreadyExist = true)
    }

    private fun getRandomSymbolFrom(type: WGSLType, mustBeWriteable: Boolean, mustAlreadyExist: Boolean): Symbol {
        val subtable = getSubtable(type)

        // inclusive
        val startIndex = if (mustBeWriteable) {
            subtable.getWriteableIndexOf(type)
        } else {
            0
        }
        // exclusive
        val endIndex = subtable.getNextIndexOf(type)

        // a blank symbol represents the need to create a new symbol,
        // or indicates no matching symbols already exist
        val generateBlankSymbol = if (mustAlreadyExist) {
            false
        } else if (mustBeWriteable) {
            PRNG.evaluateProbability(CNFG.probabilityAssignToNewSymbol)
        } else {
            false
        }

        if (startIndex == endIndex || generateBlankSymbol) {
            return Symbol("", type)
        }

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