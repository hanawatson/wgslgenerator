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

    private fun addComponentWithReturnType(symbolTable: SymbolTable, returnType: WGSLType, depth: Int) {
        components.add(ExpressionGenerator.getExpressionWithReturnType(symbolTable, returnType, depth + 1))
    }

    override fun generate(
        symbolTable: SymbolTable,
        returnType: WGSLType,
        expr: Expr,
        depth: Int
    ): IdentityConstructorExpression {
        this.returnType = returnType
        this.expr = expr

        when (returnType) {
            is WGSLVectorType -> {
                when (returnType.length) {
                    2    -> {
                        addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                        addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                    }
                    3    -> {
                        val firstLength = PRNG.getRandomIntInRange(1, 3)
                        if (firstLength == 2) {
                            addComponentWithReturnType(
                                symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                            )
                            addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                        } else {
                            val secondLength = PRNG.getRandomIntInRange(1, 3)
                            if (secondLength == 2) {
                                addComponentWithReturnType(
                                    symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                                )
                            } else {
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                            }
                        }
                    }
                    4    -> {
                        val firstLength = PRNG.getRandomIntInRange(1, 4)
                        if (firstLength == 3) {
                            addComponentWithReturnType(
                                symbolTable, WGSLVectorType(returnType.componentType, 3), depth
                            )
                            addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                        } else if (firstLength == 2) {
                            addComponentWithReturnType(
                                symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                            )
                            val secondLength = PRNG.getRandomIntInRange(1, 3)
                            if (secondLength == 2) {
                                addComponentWithReturnType(
                                    symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                                )
                            } else {
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                            }
                        } else {
                            addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                            val secondLength = PRNG.getRandomIntInRange(1, 4)
                            if (secondLength == 3) {
                                addComponentWithReturnType(
                                    symbolTable, WGSLVectorType(returnType.componentType, 3), depth
                                )
                            } else if (secondLength == 2) {
                                addComponentWithReturnType(
                                    symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                                )
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                            } else {
                                addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                                val thirdLength = PRNG.getRandomIntInRange(1, 3)
                                if (thirdLength == 2) {
                                    addComponentWithReturnType(
                                        symbolTable, WGSLVectorType(returnType.componentType, 2), depth
                                    )
                                } else {
                                    addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                                    addComponentWithReturnType(symbolTable, returnType.componentType, depth)
                                }
                            }
                        }
                    }
                    else -> throw Exception("Attempt to generate vector of unknown length ${returnType.length}!")
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