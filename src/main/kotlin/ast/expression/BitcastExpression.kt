package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.ast.numericScalarTypes
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class BitcastExpression : Expression() {
    private lateinit var arg: Expression

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): BitcastExpression {
        this.returnType = returnType
        this.expr = expr

        val castedInnerType = PRNG.getRandomTypeFrom(numericScalarTypes) as WGSLScalarType
        val castedType = when (returnType) {
            is WGSLScalarType -> castedInnerType
            is WGSLVectorType -> WGSLVectorType(castedInnerType, returnType.length)
            else              -> throw Exception(
                "Attempt to generate BitcastExpression of unknown returnType $returnType!"
            )
        }

        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, castedType, depth + 1)

        return this
    }

    override fun toString(): String {
        var bitcastExpressionString = "${expr.operator}<$returnType>($arg)"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                bitcastExpressionString = "($bitcastExpressionString)"
            }
        }

        return bitcastExpressionString
    }
}