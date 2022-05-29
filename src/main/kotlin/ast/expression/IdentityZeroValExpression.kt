package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*

internal class IdentityZeroValExpression(override val returnType: WGSLType, override var expr: Expr) : Expression {
    override var numberOfParentheses = 0

    override fun toString(): String {
        return "$returnType()"
    }

    private fun getZeroValue(type: WGSLType): ArrayList<*> {
        return when (type) {
            scalarBoolType    -> arrayListOf(false)
            scalarFloatType   -> arrayListOf(0.0f)
            is WGSLScalarType -> arrayListOf(0)
            is WGSLVectorType -> ArrayList(generateSequence {
                getZeroValue(type.componentType)
            }.take(type.length).toList())
            is WGSLMatrixType -> ArrayList(generateSequence {
                getZeroValue(WGSLVectorType(type.componentType, type.length))
            }.take(type.width).toList())
            is WGSLArrayType  -> ArrayList(generateSequence {
                getZeroValue(type.elementType)
            }.take(type.elementCountValue).toList())
            else              -> throw Exception("Attempt to evaluate zero const value of unknown type $type!")
        }
    }

    override fun getConstValue(): ArrayList<*> {
        return getZeroValue(returnType)
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            return arrayListOf(returnType)
        }
    }
}