package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLMatrixType
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityConstructorExpression : Expression() {
    private val components = ArrayList<Expression>()

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = 0

    override fun generate(
        symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int
    ): IdentityConstructorExpression {
        this.returnType = returnType
        this.expr = expr

        fun addComponentWithWidthLengthType(width: Int, length: Int, type: WGSLScalarType) {
            val componentType = if (length == 1) {
                type
            } else if (width == 1) {
                WGSLVectorType(type, length)
            } else {
                WGSLMatrixType(type, width, length)
            }
            components.add(
                ExpressionGenerator.getExpressionWithReturnType(symbolTable, componentType, depth + 1)
            )
        }

        when (returnType) {
            is WGSLVectorType -> {
                fun addComponentWithLength(length: Int) {
                    addComponentWithWidthLengthType(1, length, returnType.componentType)
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
            is WGSLMatrixType -> {
                fun addComponentWithWidthLength(width: Int, length: Int) {
                    addComponentWithWidthLengthType(width, length, returnType.componentType)
                }

                fun addComponentsWithRandomWidthsLengths() {
                    // temporarily commented due to lack of implementation in Tint
                    /*when (PRNG.getRandomIntInRange(0, 3)) {
                        0    -> {
                            addComponentWithWidthLength(returnType.width, returnType.length)
                        }*/
                    when (PRNG.getRandomIntInRange(1, 3)) {
                        1    -> {
                            for (i in 1..returnType.width) {
                                addComponentWithWidthLength(1, returnType.length)
                            }
                        }
                        else -> {
                            val numberOfScalars = returnType.width * returnType.length
                            for (i in 1..numberOfScalars) {
                                addComponentWithWidthLength(1, 1)
                            }
                        }
                    }
                }
                if (returnType.width in 2..4 && returnType.length in 2..4) {
                    addComponentsWithRandomWidthsLengths()
                } else {
                    throw Exception(
                        "Attempt to generate matrix of unknown " +
                                "width ${returnType.width}, length ${returnType.length}!"
                    )
                }
            }
            else              -> throw Exception("Attempt to generate constructible of unknown type $returnType!")
        }

        return this
    }

    override fun toString(): String {
        when (returnType) {
            is WGSLVectorType -> {
                var vectorString = "$returnType"
                if (PRNG.evaluateProbability(CNFG.probabilityOmitTypeFromComposite)) {
                    vectorString = vectorString.substring(0..3)
                }

                vectorString += "(${components[0]}"
                for (i in 1 until components.size) {
                    vectorString += ", ${components[i]}"
                }
                vectorString += ")"
                return vectorString
            }
            is WGSLMatrixType -> {
                var matrixString = "$returnType"
                if (PRNG.evaluateProbability(CNFG.probabilityOmitTypeFromComposite)) {
                    matrixString = matrixString.substring(0..5)
                }

                matrixString += "(${components[0]}"
                for (i in 1 until components.size) {
                    matrixString += ", ${components[i]}"
                }
                matrixString += ")"
                return matrixString
            }
            else              -> {
                throw Exception(
                    "Attempt to generate string representation of composite of unknown type $returnType!"
                )
            }
        }
    }
}