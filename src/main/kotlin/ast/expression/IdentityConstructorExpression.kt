package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLArrayType
import wgslsmith.wgslgenerator.ast.WGSLMatrixType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class IdentityConstructorExpression(
    symbolTable: SymbolTable, override val returnType: WGSLType, override var expr: Expr, depth: Int
) : Expression {
    private val components = ArrayList<Expression>()

    override var numberOfParentheses = 0

    init {
        val probEval = when (returnType) {
            is WGSLVectorType -> PRNG.eval(CNFG.constructVectorWithSingleValue)
            else              -> false
        }
        val argTypeList = PRNG.getRandomTypeListFrom(argsForExprType(expr, returnType, probEval))
        for (argType in argTypeList) {
            components.add(ExpressionGenerator.getExpressionWithReturnType(symbolTable, argType, depth + 1))
        }
    }

    override fun toString(): String {
        when (returnType) {
            is WGSLVectorType -> {
                var vectorString = "$returnType"
                if (PRNG.eval(CNFG.omitTypeFromCompositeConstruction)) {
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
                if (PRNG.eval(CNFG.omitTypeFromCompositeConstruction)) {
                    matrixString = matrixString.substring(0..5)
                }

                matrixString += "(${components[0]}"
                for (i in 1 until components.size) {
                    matrixString += ", ${components[i]}"
                }
                matrixString += ")"
                return matrixString
            }
            is WGSLArrayType  -> {
                var arrayString = "$returnType"

                arrayString += "(${components[0]}"
                for (i in 1 until (returnType).elementCountValue) {
                    arrayString += ", ${components[i]}"
                }
                arrayString += ")"
                return arrayString
            }
            else              -> {
                throw Exception(
                    "Attempt to generate string representation of composite of unknown type $returnType!"
                )
            }
        }
    }

    companion object : ExpressionCompanion {
        override fun argsForExprType(
            expr: Expr, returnType: WGSLType, configOption: Boolean
        ): ArrayList<ArrayList<WGSLType>> {
            // return lists of arguments that can be used to construct returnType
            val argTypeLists = ArrayList<ArrayList<WGSLType>>()

            when (returnType) {
                is WGSLVectorType -> {
                    val type = returnType.componentType
                    // construct vector with single value
                    if (configOption) {
                        argTypeLists.add(arrayListOf(type))
                    } else {
                        when (returnType.length) {
                            2 -> argTypeLists.add(arrayListOf(type, type))
                            3 -> {
                                argTypeLists.add(arrayListOf(WGSLVectorType(type, 2), type))
                                argTypeLists.add(arrayListOf(type, WGSLVectorType(type, 2)))
                            }
                            4 -> {
                                argTypeLists.add(arrayListOf(WGSLVectorType(type, 3), type))
                                argTypeLists.add(arrayListOf(type, WGSLVectorType(type, 3)))
                                argTypeLists.add(arrayListOf(WGSLVectorType(type, 2), WGSLVectorType(type, 2)))
                                argTypeLists.add(arrayListOf(type, type, WGSLVectorType(type, 2)))
                                argTypeLists.add(arrayListOf(type, WGSLVectorType(type, 2), type))
                                argTypeLists.add(arrayListOf(WGSLVectorType(type, 2), type, type))
                                argTypeLists.add(arrayListOf(type, type, type, type))
                            }
                        }
                    }
                }
                is WGSLMatrixType -> {
                    val type = returnType.componentType

                    // temporarily commented due to lack of implementation in Tint
                    // argTypeLists.add(arrayListOf(WGSLMatrixType(type, returnType.width, returnType.length)))
                    argTypeLists.add(ArrayList(generateSequence { WGSLVectorType(type, returnType.length) }
                        .take(returnType.width).toList()))
                    argTypeLists.add(ArrayList(generateSequence { type }
                        .take(returnType.width * returnType.length).toList()))
                }
                is WGSLArrayType  -> {
                    argTypeLists.add(ArrayList(generateSequence { returnType.elementType }
                        .take(returnType.elementCountValue).toList()))
                }
                else              -> throw Exception("Attempt to construct unknown type $returnType!")
            }

            return argTypeLists
        }
    }
}