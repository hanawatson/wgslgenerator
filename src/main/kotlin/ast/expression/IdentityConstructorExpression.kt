package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityConstructorExpression : Expression() {
    private val components = ArrayList<Expression>()

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr

    override fun generate(
        symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int
    ): IdentityConstructorExpression {
        this.returnType = returnType
        this.expr = expr

        when (returnType) {
            is WGSLVectorType -> {
                fun addComponentWithLength(length: Int) {
                    val componentType = if (length == 1) {
                        returnType.componentType
                    } else {
                        WGSLVectorType(returnType.componentType, length)
                    }
                    components.add(
                        ExpressionGenerator.getExpressionWithReturnType(symbolTable, componentType, depth + 1)
                    )
                }

                fun addComponentsWithRandomLengths(totalLength: Int) {
                    when (totalLength) {
                        2 -> {
                            when (PRNG.getRandomIntInRange(1, 3)) {
                                2 -> {
                                    addComponentWithLength(2)
                                }
                                1 -> {
                                    addComponentWithLength(1)
                                    addComponentWithLength(1)
                                }
                            }
                        }
                        3 -> {
                            when (PRNG.getRandomIntInRange(1, 4)) {
                                3 -> {
                                    addComponentWithLength(3)
                                }
                                2 -> {
                                    addComponentWithLength(2)
                                    addComponentWithLength(1)
                                }
                                1 -> {
                                    addComponentWithLength(1)
                                    addComponentsWithRandomLengths(2)
                                }
                            }
                        }
                        4 -> {
                            when (PRNG.getRandomIntInRange(1, 5)) {
                                4 -> {
                                    addComponentWithLength(4)
                                }
                                3 -> {
                                    addComponentWithLength(3)
                                    addComponentWithLength(1)
                                }
                                2 -> {
                                    addComponentWithLength(2)
                                    addComponentsWithRandomLengths(2)
                                }
                                1 -> {
                                    addComponentWithLength(1)
                                    addComponentsWithRandomLengths(3)
                                }
                            }
                        }
                    }
                }

                if (PRNG.evaluateProbability(CNFG.probabilityGenerateVectorWithSingleValue)) {
                    addComponentWithLength(1)
                } else {
                    when (returnType.length) {
                        2    -> addComponentsWithRandomLengths(2)
                        3    -> addComponentsWithRandomLengths(3)
                        4    -> addComponentsWithRandomLengths(4)
                        else -> throw Exception("Attempt to generate vector of unknown length ${returnType.length}!")
                    }
                }
            }
            else              -> throw Exception("Attempt to generate constructible of unknown type $returnType!")
        }

        return this
    }

    override fun toString(): String {
        if (returnType is WGSLVectorType) {
            var vectorString = "$returnType"
            if (PRNG.evaluateProbability(CNFG.probabilityOmitTypeFromConstructible)) {
                // omit the componentType, preserving "vec2"/"vec3"/"vec4" only
                vectorString = vectorString.substring(0..3)
            }

            vectorString += "(${components[0]}"
            for (i in 1 until components.size) {
                vectorString += ", ${components[i]}"
            }
            vectorString += ")"
            return vectorString
        } else {
            throw Exception(
                "Attempt to generate string representation of constructible of unknown type $returnType!"
            )
        }
    }
}