package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class AccessExpression : Expression() {
    private lateinit var arg: Expression
    private var convenienceLetters: String = ""
    private var subscriptExpression: Expression? = null

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): AccessExpression {
        this.returnType = returnType
        this.expr = expr

        when (expr) {
            AccessConvenienceExpr.CONVENIENCE          -> {
                val convenienceLettering = if (PRNG.getRandomBool()) "rgba" else "xyzw"

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

                for (i in 1..numberOfLetters) {
                    val convenienceIndex = PRNG.getRandomIntInRange(0, argType.length)
                    convenienceLetters += convenienceLettering[convenienceIndex]
                }
            }
            AccessSubscriptScalarExpr.SUBSCRIPT_SCALAR,
            AccessSubscriptVectorExpr.SUBSCRIPT_VECTOR -> {
                val argTypes = arrayListOf(
                    when (expr) {
                        is AccessSubscriptScalarExpr -> {
                            if (returnType !is WGSLScalarType) {
                                throw Exception("Attempt to generate subscript scalar access for unknown type $returnType!")
                            } else {
                                WGSLVectorType(returnType, 0)
                            }
                        }
                        is AccessSubscriptVectorExpr -> {
                            if (returnType !is WGSLVectorType) {
                                throw Exception("Attempt to generate subscript vector access for unknown type $returnType!")
                            } else {
                                WGSLMatrixType(returnType.componentType, 0, returnType.length)
                            }
                        }
                        else                         -> throw Exception(
                            "Attempt to generate subscript access for unknown Expr $expr!"
                        )
                    }
                )

                val argType = PRNG.getRandomTypeFrom(argTypes)
                arg = ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1)
                val subscriptBound = when (argType) {
                    is WGSLVectorType -> argType.length
                    is WGSLMatrixType -> argType.width
                    else              -> throw Exception(
                        "Unable to evaluate upperBound for access of unknown type $argType!"
                    )
                }

                subscriptExpression = generateSubscriptWithUpperBound(symbolTable, subscriptBound, depth)
            }
            else                                       -> throw Exception(
                "Attempt to generate AccessExpression of unknown Expr $this.expr!"
            )
        }

        return this
    }

    fun generateSubscriptWithUpperBound(symbolTable: SymbolTable, subscriptBound: Int, depth: Int): Expression {
        if (PRNG.evaluateProbability(CNFG.probabilityGenerateSubscriptAccessInBounds)) {
            return IdentityLiteralExpression().generateIntLiteralInRange(
                symbolTable, 0, subscriptBound
            )
        } else {
            var subscriptExpression = ExpressionGenerator.getExpressionWithReturnType(
                symbolTable, scalarIntType, depth + 1
            )
            if (CNFG.ensureSubscriptAccessInBounds) {
                subscriptExpression = BinaryExpression().generateModWithIntExpressions(
                    symbolTable, subscriptExpression, IdentityLiteralExpression().generateIntLiteralInRange(
                        symbolTable, subscriptBound, subscriptBound + 1
                    )
                )
            }
            return subscriptExpression
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
        } else if (subscriptExpression != null) {
            "$argString[$subscriptExpression]"
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