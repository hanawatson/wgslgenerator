package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.AccessConvenienceExpr
import wgslsmith.wgslgenerator.ast.expression.AccessSubscriptExpr
import wgslsmith.wgslgenerator.utils.CNFG
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

    var newVarLabelIndex: Int = 0

    private fun getSubtable(type: WGSLType): Subtable {
        return when (type) {
            is WGSLScalarType -> scalarSubtable
            is WGSLVectorType -> vectorSubtable
            is WGSLMatrixType -> matrixSubtable
            is WGSLArrayType  -> arraySubtable
            else              -> throw Exception("Attempt to access symbol subtable of unknown type $type!")
        }
    }

    fun hasWriteableOfAny(types: ArrayList<WGSLType>): Boolean {
        for (type in types) {
            if (hasWriteableOf(type)) {
                return true
            }
        }
        return false
    }

    fun hasWriteableOf(type: WGSLType) = !getSubtable(type).isEmpty(type, ofWriteable = true)

    fun hasAnyOf(type: WGSLType) = !getSubtable(type).isEmpty(type, ofWriteable = false) || hasWriteableOf(type)

    private fun getNextNewSymbolName() = "var$newVarLabelIndex"

    fun declareNewWriteableSymbol(type: WGSLType) =
        addSymbol(Symbol(getNextNewSymbolName(), type), writeable = true, declared = true)

    fun declareNewNonWriteableSymbol(type: WGSLType) =
        addSymbol(Symbol(getNextNewSymbolName(), type), writeable = false, declared = true)

    fun addNewNonWriteableSymbol(symbol: Symbol) =
        addSymbol(symbol, writeable = false, declared = false)

    private fun addSymbol(symbol: Symbol, writeable: Boolean, declared: Boolean): Symbol {
        val name = symbol.name
        val type = symbol.type
        getSubtable(type).addSymbol(type, symbol, writeable)
        if (declared) newVarLabelIndex++

        if (symbol !is ConstSymbol && (type is WGSLVectorType || type is WGSLMatrixType || type is WGSLArrayType)
            && CNFG.prob(AccessSubscriptExpr.SUBSCRIPT) > 0.0) {
            val componentSymbol = when (type) {
                is WGSLVectorType -> Symbol("$name[${type.length}]", type.componentType)
                is WGSLMatrixType -> Symbol("$name[${type.width}]", WGSLVectorType(type.componentType, type.length))
                is WGSLArrayType  -> Symbol("$name[${type.elementCountValue}]", type.elementType)
                else              -> throw Exception(
                    "Attempt to add recursive component symbols to SymbolTable of unknown type $type!"
                )
            }
            addSymbol(Symbol(componentSymbol.name, componentSymbol.type), writeable, false)
        }

        if (symbol !is ConstSymbol && type is WGSLVectorType && CNFG.prob(AccessConvenienceExpr.CONVENIENCE) > 0.0) {
            val componentSymbol = Symbol("$name.${type.length}v", type.componentType)
            addSymbol(Symbol(componentSymbol.name, componentSymbol.type), writeable, false)
        }

        return symbol
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
        val symbol = subtable.getSymbolAtIndex(type, randomIndex)
        if (symbol != null) {
            val subscriptRegex = "\\[(\\d+)]".toRegex()
            val subscriptString = subscriptRegex.replace(symbol.name) { matchResult ->
                "[${PRNG.getSubscriptInBound(this, matchResult.groupValues[1].toInt())}]"
            }

            val convenienceRegex = "\\.(\\d+)v".toRegex()
            val convenienceString = convenienceRegex.replace(subscriptString) { matchResult ->
                ".${PRNG.getConvenienceLetterInBound(matchResult.groupValues[1].toInt(), getRandomBool())}"
            }

            return if (symbol is ConstSymbol) {
                ConstSymbol(convenienceString, symbol.type, symbol.value)
            } else {
                Symbol(convenienceString, symbol.type)
            }
        }

        throw Exception("Out-of-bounds symbol index generated!")
    }

    internal fun copy(): SymbolTable {
        val symbolTable = SymbolTable()

        symbolTable.newVarLabelIndex = this.newVarLabelIndex

        symbolTable.scalarSubtable = this.scalarSubtable.copy()
        symbolTable.vectorSubtable = this.vectorSubtable.copy()
        symbolTable.matrixSubtable = this.matrixSubtable.copy()
        symbolTable.arraySubtable = this.arraySubtable.copy()

        return symbolTable
    }
}