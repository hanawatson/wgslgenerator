package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class AccessExpression : Expression() {
    private lateinit var accessed: Expression
    private var convenienceLetters: String = ""
    private var subscriptExpression: Expression? = null

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): AccessExpression {
        this.returnType = returnType
        this.expr = expr

        when (expr) {
            AccessConvenienceExpr.CONVENIENCE -> {
                val convenienceLettering = if (PRNG.getRandomBool()) {
                    "rgba"
                } else {
                    "xyzw"
                }

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

                val accessedType = PRNG.getRandomTypeFrom(
                    when (numberOfLetters) {
                        1, 2 -> arrayListOf(WGSLVectorType(returnInnerType, 0))
                        3    -> arrayListOf(WGSLVectorType(returnInnerType, 3), WGSLVectorType(returnInnerType, 4))
                        4    -> arrayListOf(WGSLVectorType(returnInnerType, 4))
                        else -> throw Exception(
                            "Attempt to access too many components ($numberOfLetters) in AccessExpression!"
                        )
                    }
                ) as WGSLVectorType
                accessed = ExpressionGenerator.getExpressionWithReturnType(symbolTable, accessedType, depth + 1)

                for (i in 1..numberOfLetters) {
                    val convenienceIndex = PRNG.getRandomIntInRange(0, accessedType.length)
                    convenienceLetters += convenienceLettering[convenienceIndex]
                }
            }
            AccessSubscriptExpr.SUBSCRIPT     -> {
                if (returnType !is WGSLScalarType) {
                    throw Exception("Attempt to generate subscript for unknown type $returnType!")
                }

                val accessedType = PRNG.getRandomTypeFrom(
                    arrayListOf(WGSLVectorType(returnType, 0))
                )
                accessed = ExpressionGenerator.getExpressionWithReturnType(symbolTable, accessedType, depth + 1)
                val subscriptBound = when (accessedType) {
                    is WGSLVectorType -> accessedType.length
                    else              -> throw Exception(
                        "Unable to evaluate upperBound for access of unknown type $accessedType!"
                    )
                }

                if (PRNG.evaluateProbability(CNFG.probabilityGenerateSubscriptAccessInBounds)) {
                    subscriptExpression = IdentityLiteralExpression().generateIntLiteralInRange(
                        symbolTable, 0, subscriptBound
                    )
                } else {
                    subscriptExpression = ExpressionGenerator.getExpressionWithReturnType(
                        symbolTable, scalarIntType, depth + 1
                    )
                    if (CNFG.ensureSubscriptAccessInBounds) {
                        subscriptExpression = BinaryExpression().generateModWithIntExpressions(
                            symbolTable, subscriptExpression!!, IdentityLiteralExpression().generateIntLiteralInRange(
                                symbolTable, subscriptBound, subscriptBound + 1
                            )
                        )
                    }
                }
            }
            else                              -> throw Exception(
                "Attempt to generate AccessExpression of unknown Expr $this.expr!"
            )
        }

        return this
    }

    override fun toString(): String {
        val accessString = if (convenienceLetters != "") {
            "$accessed.$convenienceLetters"
        } else if (subscriptExpression != null) {
            "$accessed[$subscriptExpression]"
        } else {
            throw Exception(
                "Attempt to generate string representation of AccessExpression without convenienceLetters or subscript!"
            )
        }

        // parentheses are necessary in all cases but IdentityExpressions
        // accessed.subExpression must also be validated as an IdentityExpression
        // to prevent non-bracketed repeated convenience accesses
        if (accessed is IdentityExpression && (accessed as IdentityExpression).subExpression is IdentityExpression) {
            return accessString
        }

        return "($accessString)"
    }
}