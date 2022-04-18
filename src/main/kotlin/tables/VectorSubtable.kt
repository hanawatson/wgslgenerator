package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType

internal class VectorSubtable : ComplexSubtable() {
    private var length2Subtable = ScalarSubtable()
    private var length3Subtable = ScalarSubtable()
    private var length4Subtable = ScalarSubtable()

    override fun getSubtable(type: WGSLType): Subtable {
        val typeLength = if (type !is WGSLVectorType) {
            // trigger attempt error message during when clause
            0
        } else {
            type.length
        }
        return when (typeLength) {
            2    -> length2Subtable
            3    -> length3Subtable
            4    -> length4Subtable
            else -> throw Exception("Attempt to access Subtable in VectorSubtable of unknown type $type!")
        }
    }

    override fun getInnerType(type: WGSLType): WGSLType {
        if (type !is WGSLVectorType) {
            throw Exception("Attempt to add Symbol in VectorSubtable of unknown type $type!")
        }
        return type.componentType
    }

    override fun copy(): VectorSubtable {
        val vectorSubtable = VectorSubtable()

        vectorSubtable.length2Subtable = this.length2Subtable.copy()
        vectorSubtable.length3Subtable = this.length3Subtable.copy()
        vectorSubtable.length4Subtable = this.length4Subtable.copy()

        return vectorSubtable
    }
}