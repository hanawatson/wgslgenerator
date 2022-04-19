package wgslsmith.wgslgenerator.ast.expression

internal interface Expr {
    val operator: String
    // val probability: Int
}

internal interface UnaryExpr : Expr

internal enum class UnaryArithmeticExpr(override val operator: String) : UnaryExpr {
    NEGATE("-");
}

internal enum class UnaryBitExpr(override val operator: String) : UnaryExpr {
    COMPLEMENT("~");
}

internal enum class UnaryLogicalExpr(override val operator: String) : UnaryExpr {
    NOT("!");
}

internal interface BinaryExpr : Expr

internal enum class BinaryArithmeticExpr(override val operator: String) : BinaryExpr {
    ADD("+"),
    DIV("/"),
    MINUS("-"),
    MOD("%"),
    MULT("*");
}

internal enum class BinaryBitExpr(override val operator: String) : BinaryExpr {
    BIT_OR("|"),
    BIT_AND("&"),
    BIT_EXCLUSIVE_OR("^");
}

internal enum class BinaryLogicalExpr(override val operator: String) : BinaryExpr {
    OR("|"),
    AND("&");
}

internal enum class BinaryLogicalScalarExpr(override val operator: String) : BinaryExpr {
    SHORT_OR("||"),
    SHORT_AND("&&");
}

internal interface ComparisonExpr : Expr

internal enum class ComparisonEqExpr(override val operator: String) : ComparisonExpr {
    EQUAL("=="),
    NOT_EQUAL("!=");
}

internal enum class ComparisonThExpr(override val operator: String) : ComparisonExpr {
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    MORE_THAN(">"),
    MORE_THAN_OR_EQUAL(">=");
}

internal interface IdentityExpr : Expr

internal enum class IdentityConstructibleExpr(override val operator: String) : IdentityExpr {
    CONSTRUCTOR("");
}

internal enum class IdentityScalarExpr(override val operator: String) : IdentityExpr {
    LITERAL("");
}

internal enum class IdentityUniversalExpr(override val operator: String) : IdentityExpr {
    SYMBOL(""),
    ZERO_VALUE("");
}

internal interface BuiltinExpr : Expr {
    val args: Int
}

internal enum class BuiltinArithmeticExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    ABS("abs", 1),
    CLAMP("clamp", 3),
    MAX("max", 2),
    MIN("min", 2);
}

internal enum class BuiltinArithmeticScalarExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    DOT("dot", 2)
}

internal enum class BuiltinFloatExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    ACOS("acos", 1),

    // temporarily commented due to lack of implementation in Tint and naga
    //ACOSH("acosh", 1),
    ASIN("asin", 1),

    // temporarily commented due to lack of implementation in Tint and naga
    //ASINH("asinh", 1),
    ATAN("atan", 1),

    // temporarily commented due to lack of implementation in Tint and naga
    //ATANH("atanh", 1),
    ATAN2("atan2", 2),
    CEIL("ceil", 1),
    COS("cos", 1),
    COSH("cosh", 1),
    DEGREES("degrees", 1),
    EXP("exp", 1),
    EXP2("exp2", 1),
    FLOOR("floor", 1),
    FMA("fma", 3),
    FRACT("fract", 1),
    INVERSE_SQRT("inverseSqrt", 1),

    // temporarily commented due to nonfunctional implementation in naga
    // LDEXP("ldexp", 2),
    LOG("log", 1),
    LOG2("log2", 1),
    MIX("mix", 3),
    POW("pow", 2),

    // temporarily commented due to lack of implementation in Tint and naga
    // QUANTIZE_TO_F16("quantizeToF16", 1),
    RADIANS("radians", 1),
    ROUND("round", 1),
    SIGN("sign", 1),
    SIN("sin", 1),
    SINH("sinh", 1),
    SMOOTHSTEP("smoothstep", 3),
    SQRT("sqrt", 1),
    STEP("step", 2),
    TAN("tan", 1),
    TANH("tanh", 1),
    TRUNC("trunc", 1);
}

internal enum class BuiltinFloatScalarExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    DISTANCE("distance", 2),
    LENGTH("length", 1);
}

internal enum class BuiltinFloatVectorExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    NORMALIZE("normalize", 1),
    REFLECT("reflect", 2);

    // temporarily commented out due to lack of implementation in naga
    //REFRACT("refract", 3);
}

internal enum class BuiltinFloatVector3Expr(override val operator: String, override val args: Int) : BuiltinExpr {
    CROSS("cross", 2);
}

internal enum class BuiltinGeneralExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    SELECT("select", 3);
}

internal enum class BuiltinIntegerExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    // temporarily commented out due to lack of implementation in naga
    // see https://github.com/gfx-rs/naga/issues/1824
    // COUNT_LEADING_ZEROS("countLeadingZeros", 1),
    COUNT_ONE_BITS("countOneBits", 1),

    // temporarily commented out due to lack of implementation in naga
    // see https://github.com/gfx-rs/naga/issues/1824
    // COUNT_TRAILING_ZEROS("countTrailingZeros", 1),

    // temporarily commented out due to issues with implementation in naga
    // see https://github.com/gfx-rs/naga/issues/1844
    // FIRST_LEADING_BIT("firstLeadingBit", 1),
    // FIRST_TRAILING_BIT("firstTrailingBit", 1),
    EXTRACT_BITS("extractBits", 3),
    INSERT_BITS("insertBits", 4),
    REVERSE_BITS("reverseBits", 1);

    // temporarily commented out until implementation of vectors is concrete
    // SHIFT_LEFT("shiftLeft", 2),
    // SHIFT_RIGHT("shiftRight", 2);
}

internal enum class BuiltinLogicalExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    ALL("all", 1),
    ANY("any", 1);
}

internal interface AccessExpr : IdentityExpr

internal enum class AccessConvenienceExpr(override val operator: String) : AccessExpr {
    CONVENIENCE("");
}

internal enum class AccessSubscriptExpr(override val operator: String) : AccessExpr {
    SUBSCRIPT("");
}

internal val allExprs = ArrayList<Expr>(
    UnaryArithmeticExpr.values().asList() +
            UnaryBitExpr.values().asList() +
            UnaryLogicalExpr.values().asList() +
            BinaryArithmeticExpr.values().asList() +
            BinaryBitExpr.values().asList() +
            BinaryLogicalExpr.values().asList() +
            BinaryLogicalScalarExpr.values().asList() +
            ComparisonEqExpr.values().asList() +
            ComparisonThExpr.values().asList() +
            IdentityConstructibleExpr.values().asList() +
            IdentityScalarExpr.values().asList() +
            IdentityUniversalExpr.values().asList() +
            BuiltinArithmeticExpr.values().asList() +
            BuiltinArithmeticScalarExpr.values().asList() +
            BuiltinFloatExpr.values().asList() +
            BuiltinFloatScalarExpr.values().asList() +
            BuiltinFloatVectorExpr.values().asList() +
            BuiltinFloatVector3Expr.values().asList() +
            BuiltinGeneralExpr.values().asList() +
            BuiltinIntegerExpr.values().asList() +
            BuiltinLogicalExpr.values().asList() +
            AccessConvenienceExpr.values().asList() +
            AccessSubscriptExpr.values().asList()
)
internal val compoundAssignableExprs = ArrayList<Expr>(
    BinaryArithmeticExpr.values().asList() +
            BinaryBitExpr.values().asList() +
            BinaryLogicalExpr.values().asList()
)