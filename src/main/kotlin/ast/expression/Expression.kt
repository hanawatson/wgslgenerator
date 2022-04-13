package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.Literal
import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLTypeEnum
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal enum class ExpressionForms(val expressionTypes: ArrayList<WGSLTypeEnum>) {
    BIN_OP_ARITHMETIC(arrayListOf(WGSLTypeEnum.FLOAT, WGSLTypeEnum.INT, WGSLTypeEnum.UNINT)),
    BIN_OP_BIT(arrayListOf(WGSLTypeEnum.INT, WGSLTypeEnum.UNINT)),
    BIN_OP_LOGICAL(arrayListOf(WGSLTypeEnum.BOOL)),
    BUILTIN_ARITHMETIC_FUNC(arrayListOf(WGSLTypeEnum.FLOAT, WGSLTypeEnum.INT, WGSLTypeEnum.UNINT)),
    BUILTIN_FLOAT_FUNC(arrayListOf(WGSLTypeEnum.FLOAT)),
    BUILTIN_INTEGER_FUNC(arrayListOf(WGSLTypeEnum.INT, WGSLTypeEnum.UNINT)),
    BUILTIN_LOGICAL_FUNC(arrayListOf(WGSLTypeEnum.BOOL)),
    COMPARISON(arrayListOf(WGSLTypeEnum.BOOL)),
    IDENTITY(ArrayList(WGSLTypeEnum.values().asList())),
    UNARY_OP_ARITHMETIC(arrayListOf(WGSLTypeEnum.FLOAT, WGSLTypeEnum.INT)),
    UNARY_OP_BIT(arrayListOf(WGSLTypeEnum.INT, WGSLTypeEnum.UNINT)),
    UNARY_OP_LOGICAL(arrayListOf(WGSLTypeEnum.BOOL));
}

object ExpressionGenerator {
    private val allExpressionForms = ArrayList(ExpressionForms.values().asList())

    fun getNewExpressionWithType(symbolTable: SymbolTable, expressionType: WGSLType): Expression {
        return getExpressionWithType(symbolTable, expressionType, 0)
    }

    fun getNewExpressionWithoutType(symbolTable: SymbolTable): Expression {
        return getExpressionFromList(symbolTable, null, allExpressionForms, 0)
    }

    fun getExpressionWithType(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int): Expression {
        if (depth >= ConfigurationManager.maxExpressionRecursion) {
            return IdentityExpression().generate(symbolTable, expressionType, depth)
        }
        val expressionForms = when (expressionType.type) {
            WGSLTypeEnum.BOOL  -> arrayListOf(
                ExpressionForms.BIN_OP_LOGICAL,
                ExpressionForms.BUILTIN_LOGICAL_FUNC,
                ExpressionForms.COMPARISON,
                ExpressionForms.IDENTITY,
                ExpressionForms.UNARY_OP_LOGICAL
            )
            WGSLTypeEnum.FLOAT -> arrayListOf(
                ExpressionForms.BIN_OP_ARITHMETIC,
                ExpressionForms.BUILTIN_ARITHMETIC_FUNC,
                ExpressionForms.BUILTIN_FLOAT_FUNC,
                ExpressionForms.IDENTITY,
                ExpressionForms.UNARY_OP_ARITHMETIC
            )
            WGSLTypeEnum.INT   -> arrayListOf(
                ExpressionForms.BIN_OP_ARITHMETIC,
                ExpressionForms.BIN_OP_BIT,
                ExpressionForms.BUILTIN_ARITHMETIC_FUNC,
                ExpressionForms.BUILTIN_INTEGER_FUNC,
                ExpressionForms.IDENTITY,
                ExpressionForms.UNARY_OP_ARITHMETIC,
                ExpressionForms.UNARY_OP_BIT
            )
            WGSLTypeEnum.UNINT -> arrayListOf(
                ExpressionForms.BIN_OP_ARITHMETIC,
                ExpressionForms.BIN_OP_BIT,
                ExpressionForms.BUILTIN_ARITHMETIC_FUNC,
                ExpressionForms.BUILTIN_INTEGER_FUNC,
                ExpressionForms.IDENTITY,
                ExpressionForms.UNARY_OP_BIT
            )
            // else               -> throw Exception("Unknown internal expression type handled!")
        }
        return getExpressionFromList(symbolTable, expressionType, expressionForms, depth)
    }

    // given a list get an index from it
    // then go to the corresponding generator/interface thing whatever.
    private fun getExpressionFromList(
        symbolTable: SymbolTable, givenExpressionType: WGSLType?, expressionForms:
        ArrayList<ExpressionForms>, depth: Int
    ): Expression {
        val formIndex = PseudoNumberGenerator.getRandomIntInRange(0, expressionForms.size)
        val expressionForm = expressionForms[formIndex]
        val expressionType = if (givenExpressionType == null) {
            val typeIndex = PseudoNumberGenerator.getRandomIntInRange(0, expressionForm.expressionTypes.size)
            val expressionTypeEnum = expressionForm.expressionTypes[typeIndex]
            // if statement: if matrix or vector etc. do something else here. else:
            WGSLScalarType(expressionTypeEnum)
        } else {
            givenExpressionType
        }

        return when (expressionForm) {
            ExpressionForms.BIN_OP_ARITHMETIC,
            ExpressionForms.BIN_OP_BIT,
            ExpressionForms.BIN_OP_LOGICAL       -> BinOpGenerator.get(
                symbolTable,
                expressionType,
                depth,
                expressionForm
            )
            ExpressionForms.BUILTIN_ARITHMETIC_FUNC,
            ExpressionForms.BUILTIN_FLOAT_FUNC,
            ExpressionForms.BUILTIN_INTEGER_FUNC,
            ExpressionForms.BUILTIN_LOGICAL_FUNC -> BuiltinFuncGenerator.get(
                symbolTable,
                expressionType,
                depth,
                expressionForm
            )
            ExpressionForms.COMPARISON           -> ComparisonGenerator.get(symbolTable, expressionType, depth)
            ExpressionForms.IDENTITY             -> IdentityExpression().generate(
                symbolTable,
                expressionType,
                depth
            )
            ExpressionForms.UNARY_OP_ARITHMETIC,
            ExpressionForms.UNARY_OP_BIT,
            ExpressionForms.UNARY_OP_LOGICAL     -> UnOpGenerator.get(
                symbolTable,
                expressionType,
                depth,
                expressionForm
            )
        }
    }

    fun getLiteralAsExpression(literal: Literal): Expression {
        return IdentityExpression().getLiteralAsIdentity(literal)
    }
}

abstract class Expression {
    abstract var expressionType: WGSLType

    abstract fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int): Expression

    fun getType(): WGSLType {
        return expressionType
    }
}