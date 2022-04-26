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
        val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType))
        val length = if (argType is WGSLVectorType) {
            argType.length
        } else {
            (returnType as WGSLVectorType).length
        }
        sign = if (PRNG.getRandomBool()) "s" else "u"
        if (length == 2) {
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

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                dataExpressionString = "($dataExpressionString)"
            }
        }

        return dataExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            val argTypes = ArrayList<WGSLType>()
            when (expr) {
                is DataPackExpr   -> {
                    argTypes.add(vector2FloatType)
                    argTypes.add(vector4FloatType)
                }
                is DataUnpackExpr -> {
                    argTypes.add(scalarUnIntType)
                }
                else              -> throw Exception(
                    "Attempt to generate DataExpression argType of unknown returnType $returnType!"
                )
            }

            return argTypes
        }
    }
}