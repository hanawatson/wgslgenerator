package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class ConversionExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var arg: Expression

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        val innerTypes = if (expr is ConversionBitcastExpr) numericScalarTypes else scalarTypes
        val argInnerType = PRNG.getRandomTypeFrom(innerTypes) as WGSLScalarType
        val argType = when (returnType) {
            is WGSLScalarType -> argInnerType
            is WGSLVectorType -> WGSLVectorType(argInnerType, returnType.length)
            else              -> throw Exception(
                "Attempt to generate ConversionExpression of unknown returnType $returnType!"
            )
        }
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
    }

    override fun toString(): String {
        var conversionExpressionString = if (expr is ConversionBitcastExpr) {
            "${expr.operator}<$returnType>($arg)"
        } else {
            "$returnType($arg)"
        }
        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                conversionExpressionString = "($conversionExpressionString)"
            }
        }

        return conversionExpressionString
    }
}