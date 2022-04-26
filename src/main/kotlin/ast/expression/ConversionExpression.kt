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
        val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType))
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
    }

    override fun toString(): String {
        var conversionExpressionString = if (expr is ConversionBitcastExpr) {
            "${expr.operator}<$returnType>($arg)"
        } else {
            "$returnType($arg)"
        }
        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                conversionExpressionString = "($conversionExpressionString)"
            }
        }

        return conversionExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            val convertibleTypes = if (expr is ConversionBitcastExpr) numericScalarTypes else scalarTypes

            val argTypes = ArrayList<WGSLType>()
            for (convertibleType in convertibleTypes) {
                argTypes.add(
                    when (returnType) {
                        is WGSLScalarType -> convertibleType
                        is WGSLVectorType -> WGSLVectorType(convertibleType as WGSLScalarType, returnType.length)
                        else              -> throw Exception(
                            "Attempt to generate ConversionExpression argTypes of unknown returnType $returnType!"
                        )
                    }
                )
            }

            return argTypes
        }
    }
}