package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.WGSLMatrixType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType

internal class MatrixSubtable : ComplexSubtable() {
    private var width2Subtable = VectorSubtable()
    private var width3Subtable = VectorSubtable()
    private var width4Subtable = VectorSubtable()

    override fun getSubtable(type: WGSLType): Subtable {
        val typeWidth = if (type !is WGSLMatrixType) {
            // trigger attempt error message during when clause
            0
        } else {
            type.width
        }
        return when (typeWidth) {
            2    -> width2Subtable
            3    -> width3Subtable
            4    -> width4Subtable
            else -> throw Exception("Attempt to access Subtable in MatrixSubtable of unknown type $type!")
        }
    }

    override fun getInnerType(type: WGSLType): WGSLType {
        if (type !is WGSLMatrixType) {
            throw Exception("Attempt to add Symbol in MatrixSubtable of unknown type $type!")
        }
        return WGSLVectorType(type.componentType, type.length)
    }

    override fun copy(): MatrixSubtable {
        val matrixSubtable = MatrixSubtable()

        matrixSubtable.width2Subtable = this.width2Subtable.copy()
        matrixSubtable.width3Subtable = this.width3Subtable.copy()
        matrixSubtable.width4Subtable = this.width4Subtable.copy()

        return matrixSubtable
    }
}