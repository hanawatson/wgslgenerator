package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.PRNG
import wgslsmith.wgslgenerator.utils.PRNG.getRandomBool

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
    private var arraySubtable = ArraySubtable(0)

    private var newVarLabelIndex: Int = 0

    private fun getSubtable(type: WGSLType): Subtable {
        return when (type) {
            is WGSLScalarType -> scalarSubtable
            is WGSLVectorType -> vectorSubtable
            is WGSLMatrixType -> matrixSubtable
            is WGSLArrayType  -> arraySubtable
            else              -> throw Exception("Attempt to access symbol subtable of unknown type $type!")
        }
    }

    fun hasWriteableOf(type: WGSLType) = !getSubtable(type).isEmpty(type, ofWriteable = true)

    private fun getNextNewSymbolName() = "var$newVarLabelIndex"

    fun declareNewWriteableSymbol(type: WGSLType) =
        addSymbol(getNextNewSymbolName(), type, writeable = true, declared = true)

    fun declareNewNonWriteableSymbol(type: WGSLType) =
        addSymbol(getNextNewSymbolName(), type, writeable = false, declared = true)

    fun addNewNonWriteableSymbol(name: String, type: WGSLType) =
        addSymbol(name, type, writeable = false, declared = false)

    private fun addSymbol(name: String, type: WGSLType, writeable: Boolean, declared: Boolean): Symbol {
        val rootSymbol = Symbol(name, type)
        var symbol = rootSymbol
        getSubtable(rootSymbol.type).addSymbol(rootSymbol.type, rootSymbol, writeable)
        if (declared) newVarLabelIndex++

        while (symbol.type is WGSLVectorType || symbol.type is WGSLMatrixType || symbol.type is WGSLArrayType) {
            val componentSymbol = when (val symbolType = symbol.type) {
                is WGSLVectorType -> Symbol("${symbol.name}[${symbolType.length}v]", symbolType.componentType)
                is WGSLMatrixType -> Symbol(
                    "${symbol.name}[${symbolType.width}]", WGSLVectorType(symbolType.componentType, symbolType.length)
                )
                is WGSLArrayType  -> Symbol("${symbol.name}[${symbolType.elementCountValue}]", symbolType.elementType)
                else              -> throw Exception(
                    "Attempt to add recursive component symbols to SymbolTable of unknown type ${symbol.type}!"
                )
            }
            getSubtable(componentSymbol.type).addSymbol(componentSymbol.type, componentSymbol, writeable)
            symbol = componentSymbol
        }

        return rootSymbol
    }

    fun getRandomSymbol(type: WGSLType): Symbol = getRandomSymbolFrom(type, mustBeWriteable = false)

    fun getRandomWriteableSymbol(type: WGSLType): Symbol = getRandomSymbolFrom(type, mustBeWriteable = true)

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
            val symbol = subtable.getSymbolAtIndex(type, randomIndex)!!
            val subscriptRegex = "\\[(\\d+v?)]".toRegex()
            val subscriptString = subscriptRegex.replace(symbol.name) { matchResult ->
                var subscriptBoundString = matchResult.groupValues[1]
                val useConvenienceLetter = subscriptBoundString.contains("v")
                if (useConvenienceLetter) subscriptBoundString = subscriptBoundString.replace("v", "")
                if (useConvenienceLetter && getRandomBool()) {
                    ".${PRNG.getConvenienceLetterInBound(subscriptBoundString.toInt(), getRandomBool())}"
                } else {
                    "[${PRNG.getSubscriptInBound(this, subscriptBoundString.toInt())}]"
                }
            }
            return Symbol(subscriptString, symbol.type)
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