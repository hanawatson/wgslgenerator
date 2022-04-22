package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.CNFG

internal class ArraySubtable(private val recursionDepth: Int) : ComplexSubtable() {
    private var subtablesList: ArrayList<ArrayList<Subtable>> = run {
        val subtablesListInitializer = ArrayList<ArrayList<Subtable>>()
        for (i in 1..CNFG.maxArrayElementCount) {
            val subtables: ArrayList<Subtable> = arrayListOf(ScalarSubtable(), VectorSubtable(), MatrixSubtable())
            if (recursionDepth < CNFG.maxArrayRecursion) {
                subtables.add(ArraySubtable(recursionDepth + 1))
            }
            subtablesListInitializer.add(subtables)
        }
        subtablesListInitializer
    }
    private val scalarSubtableIndex = 0
    private val vectorSubtableIndex = 1
    private val matrixSubtableIndex = 2
    private val arraySubtableIndex = 3

    override fun getSubtable(type: WGSLType): Subtable {
        val typeElementCountValue = if (type !is WGSLArrayType) {
            // trigger attempt error message during when clause
            0
        } else {
            type.elementCountValue
        }
        if (typeElementCountValue > 0) {
            return when (val typeElementType = (type as WGSLArrayType).elementType) {
                is WGSLScalarType -> subtablesList[typeElementCountValue - 1][scalarSubtableIndex]
                is WGSLVectorType -> subtablesList[typeElementCountValue - 1][vectorSubtableIndex]
                is WGSLMatrixType -> subtablesList[typeElementCountValue - 1][matrixSubtableIndex]
                is WGSLArrayType  -> subtablesList[typeElementCountValue - 1][arraySubtableIndex]
                else              -> throw Exception(
                    "Attempt to access Subtable in ArraySubtable of unknown elementType $typeElementType!"
                )
            }
        } else {
            throw Exception(
                "Attempt to access SubtablesList in ArraySubtable of unknown elementCountValue $typeElementCountValue!"
            )
        }
    }

    override fun getInnerType(type: WGSLType): WGSLType {
        if (type !is WGSLArrayType) {
            throw Exception("Attempt to add Symbol in ArraySubtable of unknown type $type!")
        }
        return type.elementType
    }

    override fun copy(): ArraySubtable {
        val arraySubtable = ArraySubtable(recursionDepth + 1)

        val subtablesListCopy = ArrayList<ArrayList<Subtable>>()
        for (subtableList in subtablesList) {
            val subtableListCopy = ArrayList<Subtable>()
            for (subtable in subtableList) {
                subtableListCopy.add(subtable.copy())
            }
            subtablesListCopy.add(subtableListCopy)
        }

        return arraySubtable
    }
}