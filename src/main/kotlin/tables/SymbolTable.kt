package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

class SymbolTable {
    private var boolSubtable: TypeSubtable = TypeSubtable(WGSLScalarType(Type.BOOL))
    private var floatSubtable: TypeSubtable = TypeSubtable(WGSLScalarType(Type.FLOAT))
    private var intSubtable: TypeSubtable = TypeSubtable(WGSLScalarType(Type.INT))
    private var unIntSubtable: TypeSubtable = TypeSubtable(WGSLScalarType(Type.UNINT))

    private var newVarLabelIndex: Int = 0

    private fun getTypeSubtable(type: WGSLType): TypeSubtable {
        return when (type.type) {
            Type.BOOL  -> boolSubtable
            Type.FLOAT -> floatSubtable
            Type.INT   -> intSubtable
            Type.UNINT -> unIntSubtable
            // else               -> throw Exception("Attempt to access symbol subtable of unknown type!")
        }
    }

    fun hasWriteableOf(type: WGSLType): Boolean {
        return !getTypeSubtable(type).isEmpty(ofWriteable = true)
    }

    private fun getNextNewSymbolName(): String {
        return "var$newVarLabelIndex"
    }

    fun declareNewSymbol(type: WGSLType): Symbol {
        val newSymbol = Symbol(getNextNewSymbolName(), type)
        addSymbol(newSymbol)
        newVarLabelIndex++
        return newSymbol
    }

    private fun addSymbol(symbol: Symbol) {
        getTypeSubtable(symbol.getType()).addSymbol(symbol)
    }

    fun addNonWriteableSymbol(symbol: Symbol) {
        getTypeSubtable(symbol.getType()).addNonWriteableSymbol(symbol)
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
        val subtable = getTypeSubtable(type)

        // inclusive
        val startIndex = if (mustBeWriteable) {
            subtable.getWriteableIndex()
        } else {
            0
        }
        // exclusive
        val endIndex = subtable.getNextIndex()

        // a blank symbol represents new symbol if this must be writeable, literal otherwise
        val generateBlankSymbol = if (mustAlreadyExist) {
            false
        } else {
            PseudoNumberGenerator.evaluateProbability(
                if (mustBeWriteable) {
                    ConfigurationManager.probabilityAssignToNewSymbol
                } else {
                    ConfigurationManager.probabilityAssignLiteral
                }
            )
        }

        if (startIndex == endIndex || generateBlankSymbol) {
            return Symbol("", type)
        }

        // get random index
        // a result of startIndex-blankSymbolIndices means a blank symbol (i.e. a new one/a literal) is returned
        val randomIndex = PseudoNumberGenerator.getRandomIntInRange(startIndex, endIndex)
        if (subtable.getSymbolAtIndex(randomIndex) != null) {
            return subtable.getSymbolAtIndex(randomIndex)!!
        }

        throw Exception("Out-of-bounds symbol index generated!")
    }

    internal fun copy(): SymbolTable {
        val symbolTable = SymbolTable()

        symbolTable.boolSubtable = this.boolSubtable.copy()
        symbolTable.floatSubtable = this.intSubtable.copy()
        symbolTable.intSubtable = this.intSubtable.copy()
        symbolTable.unIntSubtable = this.unIntSubtable.copy()

        symbolTable.newVarLabelIndex = this.newVarLabelIndex

        return symbolTable
    }
}