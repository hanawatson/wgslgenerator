package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG
import wgslsmith.wgslgenerator.utils.PRNG.getRandomBool

internal class AccessExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private var arg: Expression
    private var convenienceLetters: String = ""
    private var subscriptExpressionString: String = ""

    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    init {
        when (expr) {
            is AccessConvenienceExpr -> {
                val numberOfLetters = when (returnType) {
                    is WGSLScalarType -> {
                        1
                    }
                    is WGSLVectorType -> {
                        returnType.length
                    }
                    else              -> throw Exception(
                        "Attempt to generate convenienceLetters for unknown type $returnType!"
                    )
                }

                val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType)) as WGSLVectorType
                arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)

                val useRGBA = getRandomBool()
                for (i in 1..numberOfLetters) {
                    convenienceLetters += PRNG.getConvenienceLetterInBound(argType.length, useRGBA)
                }
            }
            is AccessSubscriptExpr   -> {
                val argType = PRNG.getRandomTypeFrom(argsForExprType(expr, returnType))
                arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
                val subscriptBound = when (argType) {
                    is WGSLVectorType -> argType.length
                    is WGSLMatrixType -> argType.width
                    is WGSLArrayType  -> argType.elementCountValue
                    else              -> throw Exception(
                        "Unable to evaluate subscriptBound for access of unknown type $argType!"
                    )
                }

                subscriptExpressionString = PRNG.getSubscriptInBoundAtDepth(symbolTable, subscriptBound, depth)
            }
            else                     -> throw Exception(
                "Attempt to generate AccessExpression of unknown Expr $this.expr!"
            )
        }
    }

    override fun toString(): String {
        // parentheses are necessary in all cases but IdentityExpressions/AccessExpressions that are not conveniences
        // accessed.subExpression must also be validated as an IdentityExpression/non-convenience AccessExpression
        // to prevent repeated convenience accesses (e.g. vectorVar.xyz.xyz, which will error) from the subexpression
        val argString = if (CNFG.useNecessaryExpressionParentheses && (
                    (arg is IdentityExpression && ((arg as IdentityExpression).subExpression == null
                            || (arg as IdentityExpression).subExpression!!.expr != AccessConvenienceExpr.CONVENIENCE))
                            || (arg is AccessExpression
                            && (arg as AccessExpression).expr != AccessConvenienceExpr.CONVENIENCE))) {
            "$arg"
        } else {
            "($arg)"
        }

        var accessExpressionString = if (convenienceLetters != "") {
            "$argString.$convenienceLetters"
        } else if (subscriptExpressionString != "") {
            "$argString[$subscriptExpressionString]"
        } else {
            throw Exception(
                "Attempt to generate string representation of AccessExpression without convenienceLetters or subscript!"
            )
        }

        if (CNFG.useExcessExpressionParentheses) {
            for (i in 0 until numberOfParentheses) {
                accessExpressionString = "($accessExpressionString)"
            }
        }

        return accessExpressionString
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<WGSLType> {
            val argTypes = ArrayList<WGSLType>()

            when (expr) {
                is AccessConvenienceExpr -> {
                    val length: Int
                    val returnInnerType: WGSLType
                    when (returnType) {
                        is WGSLScalarType -> {
                            length = 1
                            returnInnerType = returnType
                        }
                        is WGSLVectorType -> {
                            length = returnType.length
                            returnInnerType = returnType.componentType
                        }
                        else              -> throw Exception(
                            "Attempt to generate argTypes for unknown type $returnType!"
                        )
                    }

                    when (length) {
                        1, 2 -> argTypes.add(WGSLVectorType(returnInnerType, 0))
                        3    -> {
                            argTypes.add(WGSLVectorType(returnInnerType, 3))
                            argTypes.add(WGSLVectorType(returnInnerType, 4))
                        }
                        4    -> argTypes.add(WGSLVectorType(returnInnerType, 4))
                    }
                }
                is AccessSubscriptExpr   -> {
                    when (returnType) {
                        is WGSLScalarType -> argTypes.addAll(
                            arrayListOf(
                                WGSLVectorType(returnType, 0),
                                WGSLArrayType(returnType, IdentityLiteralExpression(0), 0)
                            )
                        )
                        is WGSLVectorType -> {
                            argTypes.add(WGSLArrayType(returnType, IdentityLiteralExpression(0), 0))
                            if (returnType.componentType == scalarFloatType) {
                                argTypes.add(WGSLMatrixType(returnType.componentType, 0, returnType.length))
                            }
                        }
                        is WGSLMatrixType -> argTypes.add(
                            WGSLArrayType(returnType, IdentityLiteralExpression(0), 0)
                        )
                        is WGSLArrayType  -> {
                            if (returnType.nestedDepth >= CNFG.maxArrayNestDepth) {
                                throw Exception(
                                    "Attempt to generate argTypes for WGSLArrayType of invalid nestedDepth " +
                                            "${returnType.nestedDepth}!"
                                )
                            }
                            argTypes.add(
                                WGSLArrayType(
                                    returnType, IdentityLiteralExpression(0), returnType.nestedDepth - 1
                                )
                            )
                        }
                    }
                }
            }

            return argTypes
        }
    }
}