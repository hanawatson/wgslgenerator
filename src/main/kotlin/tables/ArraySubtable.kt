package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.utils.CNFG

internal class ArraySubtable(private val recursionDepth: Int) : ComplexSubtable() {
    private var subtablesList: ArrayList<ArrayList<Subtable>> = run {
        val subtablesListInitializer = ArrayList<ArrayList<Subtable>>()
        for (i in 1 until CNFG.maxArrayElementCount) {
            val subtables: ArrayList<Subtable> = arrayListOf(ScalarSubtable(), VectorSubtable(), MatrixSubtable())
            if (recursionDepth < CNFG.maxArrayNestDepth) {
                subtables.add(ArraySubtable(recursionDepth + 1))
            }
            subtablesListInitializer.add(subtables)
        }
        subtablesListInitializer
    }

    override fun getSubtable(type: WGSLType): Subtable {
        val typeElementCountValue = if (type !is WGSLArrayType) {
            // trigger attempt error message during when clause
            0
        } else {
            type.elementCountValue
        }
        if (typeElementCountValue > 0) {
            return when (val typeElementType = (type as WGSLArrayType).elementType) {
                is WGSLScalarType -> subtablesList[typeElementCountValue - 1][0]
                is WGSLVectorType -> subtablesList[typeElementCountValue - 1][1]
                is WGSLMatrixType -> subtablesList[typeElementCountValue - 1][2]
                is WGSLArrayType  -> subtablesList[typeElementCountValue - 1][3]
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
        val arraySubtable = ArraySubtable(recursionDepth)

        val subtablesListCopy = ArrayList<ArrayList<Subtable>>()
        for (subtableList in subtablesList) {
            val subtableListCopy = ArrayList<Subtable>()
            for (subtable in subtableList) {
                subtableListCopy.add(subtable.copy())
            }
            subtablesListCopy.add(subtableListCopy)
        }

        arraySubtable.subtablesList = subtablesListCopy

        return arraySubtable
    }
}