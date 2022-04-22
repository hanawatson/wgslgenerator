package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class DataExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var arg: Expression
    private var sign: String
    private var size: String
    private var norm: String

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        val argType: WGSLType
        val sizeType: WGSLVectorType
        when (expr) {
            is DataPackExpr   -> {
                argType = PRNG.getRandomTypeFrom(arrayListOf(vector2FloatType, vector4FloatType))
                sizeType = argType as WGSLVectorType
            }
            is DataUnpackExpr -> {
                argType = scalarUnIntType
                sizeType = returnType as WGSLVectorType
            }
            else              -> throw Exception(
                "Attempt to generate DataExpression of unknown returnType $returnType!"
            )
        }
        sign = if (PRNG.getRandomBool()) "s" else "u"
        if (sizeType.length == 2) {
            size = "2x16"
            if (PRNG.getRandomBool()) {
                sign = ""
                norm = "float"
            } else {
                norm = "norm"
            }
        } else {
            size = "4x8"
            norm = "norm"
        }
        arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
    }

    override fun toString(): String {
        var dataExpressionString = "${expr.operator}$size$sign$norm($arg)"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                dataExpressionString = "($dataExpressionString)"
            }
        }

        return dataExpressionString
    }
}