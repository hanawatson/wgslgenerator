package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG
import wgslsmith.wgslgenerator.utils.PRNG.getRandomBool

internal class AccessExpression : Expression {
    private lateinit var arg: Expression
    private var convenienceLetters: String = ""
    private var subscriptExpressionString: String = ""

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): AccessExpression {
        this.returnType = returnType
        this.expr = expr

        when (expr) {
            is AccessConvenienceExpr -> {
                val numberOfLetters: Int
                val returnInnerType: WGSLType
                when (returnType) {
                    is WGSLScalarType -> {
                        numberOfLetters = 1
                        returnInnerType = returnType
                    }
                    is WGSLVectorType -> {
                        numberOfLetters = returnType.length
                        returnInnerType = returnType.componentType
                    }
                    else              -> throw Exception(
                        "Attempt to generate convenienceLetters for unknown type $returnType!"
                    )
                }

                val argType = PRNG.getRandomTypeFrom(
                    when (numberOfLetters) {
                        1, 2 -> arrayListOf(WGSLVectorType(returnInnerType, 0))
                        3    -> arrayListOf(WGSLVectorType(returnInnerType, 3), WGSLVectorType(returnInnerType, 4))
                        4    -> arrayListOf(WGSLVectorType(returnInnerType, 4))
                        else -> throw Exception(
                            "Attempt to access too many components ($numberOfLetters) in AccessExpression!"
                        )
                    }
                ) as WGSLVectorType
                arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)

                val useRGBA = getRandomBool()
                for (i in 1..numberOfLetters) {
                    convenienceLetters += PRNG.getConvenienceLetterInBound(argType.length, useRGBA)
                }
            }
            is AccessSubscriptExpr   -> {
                val argTypes: ArrayList<WGSLType> = when (returnType) {
                    is WGSLScalarType -> arrayListOf(
                        WGSLVectorType(returnType, 0),
                        WGSLArrayType(returnType, IdentityLiteralExpression().generateIntLiteral(0), 0)
                    )
                    is WGSLVectorType -> {
                        val accessibleFrom: ArrayList<WGSLType> = arrayListOf(
                            WGSLArrayType(returnType, IdentityLiteralExpression().generateIntLiteral(0), 0)
                        )
                        if (returnType.componentType == scalarFloatType) {
                            accessibleFrom.add(WGSLMatrixType(returnType.componentType, 0, returnType.length))
                        }
                        accessibleFrom
                    }
                    is WGSLMatrixType -> arrayListOf(
                        WGSLArrayType(returnType, IdentityLiteralExpression().generateIntLiteral(0), 0)
                    )
                    is WGSLArrayType  -> {
                        if (returnType.nestedDepth >= CNFG.maxArrayRecursion) {
                            throw Exception("bruh")
                        }
                        arrayListOf(
                            WGSLArrayType(
                                returnType,
                                IdentityLiteralExpression().generateIntLiteral(0),
                                returnType.nestedDepth - 1
                            )
                        )
                    }
                    else              -> throw Exception(
                        "Attempt to generate subscript access for unknown type $returnType!"
                    )
                }

                val argType = PRNG.getRandomTypeFrom(argTypes)
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

        return this
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

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                accessExpressionString = "($accessExpressionString)"
            }
        }

        return accessExpressionString
    }
}