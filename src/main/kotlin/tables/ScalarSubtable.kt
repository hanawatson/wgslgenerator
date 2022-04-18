package wgslsmith.wgslgenerator.tables

import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType

internal class ScalarSubtable : ComplexSubtable() {
    private var boolSubtable = SimpleSubtable()
    private var floatSubtable = SimpleSubtable()
    private var intSubtable = SimpleSubtable()
    private var unIntSubtable = SimpleSubtable()

    override fun getSubtable(type: WGSLType): Subtable {
        return when (type.type) {
            Type.BOOL  -> boolSubtable
            Type.FLOAT -> floatSubtable
            Type.INT   -> intSubtable
            Type.UNINT -> unIntSubtable
            else       -> throw Exception("Attempt to access Subtable in ScalarSubtable of unknown type $type!")
        }
    }

    override fun getInnerType(type: WGSLType): WGSLScalarType {
        if (type !is WGSLScalarType) {
            throw Exception("Attempt to add Symbol in ScalarSubtable of unknown type $type!")
        }
        return type
    }

    override fun copy(): ScalarSubtable {
        val scalarSubtable = ScalarSubtable()

        scalarSubtable.boolSubtable = this.boolSubtable.copy()
        scalarSubtable.floatSubtable = this.floatSubtable.copy()
        scalarSubtable.intSubtable = this.intSubtable.copy()
        scalarSubtable.unIntSubtable = this.unIntSubtable.copy()

        return scalarSubtable
    }
}