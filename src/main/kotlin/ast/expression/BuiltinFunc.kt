package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLScalarType
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLTypeEnum
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

interface BuiltinFuncForms {
    val funcName: String
    val args: Int
}

internal enum class BuiltinArithmeticFuncForms(override val funcName: String, override val args: Int) :
    BuiltinFuncForms {
    ABS("abs", 1),
    CLAMP("clamp", 3),
    MAX("max", 2),
    MIN("min", 2);
}

internal enum class BuiltinFloatFuncForms(override val funcName: String, override val args: Int) : BuiltinFuncForms {
    ACOS("acos", 1),
    ASIN("asin", 1),
    ATAN("atan", 1),
    ATAN2("atan2", 2),
    CEIL("ceil", 1),
    COS("cos", 1),
    COSH("cosh", 1),
    DEGREES("degrees", 1),
    DISTANCE("distance", 2),
    EXP("exp", 1),
    EXP2("exp2", 1),
    FLOOR("floor", 1),
    FMA("fma", 3),
    FRACT("fract", 1),
    INVERSE_SQRT("inverseSqrt", 1),
    LDEXP("ldexp", 2),
    LENGTH("length", 1),
    LOG("log", 1),
    LOG2("log2", 1),
    MIX("mix", 3),
    POW("pow", 2),
    QUANTIZE_TO_F16("quantizeToF16", 1),
    RADIANS("radians", 1),
    REFLECT("reflect", 2),
    REFRACT("refract", 3),
    ROUND("round", 1),
    SIGN("sign", 1),
    SIN("sin", 1),
    SINH("sinh", 1),
    SMOOTH_STEP("smoothStep", 3),
    SQRT("sqrt", 1),
    STEP("step", 2),
    TAN("tan", 1),
    TANH("tanh", 1),
    TRUNC("trunc", 1);
}

internal enum class BuiltinIntegerFuncForms(override val funcName: String, override val args: Int) : BuiltinFuncForms {
    COUNT_LEADING_ZEROS("countLeadingZeros", 1),
    COUNT_ONE_BITS("countOneBits", 1),
    COUNT_TRAILING_ZEROS("countTrailingZeros", 1),
    FIRST_LEADING_BIT("firstLeadingBit", 1),
    FIRST_TRAILING_BIT("firstTrailingBit", 1),
    EXTRACT_BITS("extractBits", 3),
    INSERT_BITS("insertBits", 4),
    REVERSE_BITS("reverseBits", 1);
}

internal enum class BuiltinLogicalFuncForms(override val funcName: String, override val args: Int) : BuiltinFuncForms {
    ALL("all", 1),
    ANY("any", 1);
}

internal object BuiltinFuncGenerator {
    private val builtinArithmeticFuncForms = ArrayList<BuiltinFuncForms>(BuiltinArithmeticFuncForms.values().asList())
    private val builtinFloatFuncForms = ArrayList<BuiltinFuncForms>(BuiltinFloatFuncForms.values().asList())
    private val builtinIntegerFuncForms = ArrayList<BuiltinFuncForms>(BuiltinIntegerFuncForms.values().asList())
    private val builtinLogicalFuncForms = ArrayList<BuiltinFuncForms>(BuiltinLogicalFuncForms.values().asList())

    fun get(
        symbolTable: SymbolTable,
        expressionType: WGSLType,
        depth: Int,
        funcType: ExpressionForms
    ): Expression {
        val builtinFuncForms: ArrayList<BuiltinFuncForms> = when (funcType) {
            ExpressionForms.BUILTIN_ARITHMETIC_FUNC -> builtinArithmeticFuncForms
            ExpressionForms.BUILTIN_FLOAT_FUNC      -> builtinFloatFuncForms
            ExpressionForms.BUILTIN_INTEGER_FUNC    -> builtinIntegerFuncForms
            ExpressionForms.BUILTIN_LOGICAL_FUNC    -> builtinLogicalFuncForms
            else                                    -> builtinArithmeticFuncForms
        }

        val builtinFuncFormIndex = PseudoNumberGenerator.getRandomIntInRange(
            0, builtinFuncForms.size
        )
        val builtinFuncForm = builtinFuncForms[builtinFuncFormIndex]
        return BuiltinFuncExpression(builtinFuncForm).generate(
            symbolTable,
            expressionType,
            depth
        )
    }
}

private class BuiltinFuncExpression(private val builtinFuncForm: BuiltinFuncForms) : Expression() {
    override lateinit var expressionType: WGSLType

    private var argsLimit = 0
    private val args = ArrayList<Expression>()
    private val argTypes = ArrayList<WGSLType>()

    override fun generate(symbolTable: SymbolTable, expressionType: WGSLType, depth: Int):
            BuiltinFuncExpression {
        argsLimit = builtinFuncForm.args - 1
        for (i in (0..argsLimit)) {
            argTypes.add(expressionType)
        }

        // set integer type. will need to be changed when vecs are implemented
        val matchingSignedIntType = WGSLScalarType(WGSLTypeEnum.INT)
        val scalarUnsignedIntType = WGSLScalarType(WGSLTypeEnum.UNINT)

        // allow for irregular/diff type args.
        when (builtinFuncForm) {
            BuiltinFloatFuncForms.LDEXP          -> {
                argTypes[1] = matchingSignedIntType
            }
            BuiltinFloatFuncForms.REFRACT        -> {
                argTypes[2] = matchingSignedIntType
            }
            BuiltinIntegerFuncForms.EXTRACT_BITS -> {
                argTypes[1] = scalarUnsignedIntType
                argTypes[2] = scalarUnsignedIntType
            }
            BuiltinIntegerFuncForms.INSERT_BITS  -> {
                argTypes[2] = scalarUnsignedIntType
                argTypes[3] = scalarUnsignedIntType
            }
        }

        for (i in (0..argsLimit)) {
            val argExpression = ExpressionGenerator.getExpressionWithType(symbolTable, argTypes[i], depth + 1)
            args.add(argExpression)
        }

        this.expressionType = expressionType

        return this
    }

    override fun toString(): String {
        var funcString = "${builtinFuncForm.funcName}(${args[0]}"
        for (i in 1..argsLimit) {
            funcString += ", ${args[i]}"
        }
        funcString += ")"

        return funcString
    }
}