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

// when vectors are introduced maybe make another enum with only OR / AND? then pick from there if type vec<bool>
// actually we can add together ops because we are making arraylists. no duped enums!

internal enum class BinaryLogicalExpr(override val operator: String) : BinaryExpr {
    SHORT_OR("||"),
    SHORT_AND("&&"),
    OR("|"),
    AND("&");
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

internal enum class IdentityExpr(override val operator: String) : Expr {
    ID("");
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

internal enum class BuiltinFloatExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    ACOS("acos", 1),
    ACOSH("acosh", 1),
    ASIN("asin", 1),
    ASINH("asinh", 1),
    ATAN("atan", 1),
    ATANH("atanh", 1),
    ATAN2("atan2", 2),
    CEIL("ceil", 1),
    COS("cos", 1),
    COSH("cosh", 1),
    DEGREES("degrees", 1),

    // temporarily commented out until implementation of vectors is concrete
    // DISTANCE("distance", 2),
    EXP("exp", 1),
    EXP2("exp2", 1),
    FLOOR("floor", 1),
    FMA("fma", 3),
    FRACT("fract", 1),
    INVERSE_SQRT("inverseSqrt", 1),

    // temporarily commented out until implementation of vectors is concrete
    // LDEXP("ldexp", 2),
    LENGTH("length", 1),
    LOG("log", 1),
    LOG2("log2", 1),
    MIX_COMPONENT("mix", 3),
    MIX_LINEAR("mix", 3),
    POW("pow", 2),

    // temporarily commented due to lack of implementation in Tint and naga
    // QUANTIZE_TO_F16("quantizeToF16", 1),
    RADIANS("radians", 1),
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

internal enum class BuiltinIntegerExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    COUNT_LEADING_ZEROS("countLeadingZeros", 1),
    COUNT_ONE_BITS("countOneBits", 1),
    COUNT_TRAILING_ZEROS("countTrailingZeros", 1),
    FIRST_LEADING_BIT("firstLeadingBit", 1),
    FIRST_TRAILING_BIT("firstTrailingBit", 1),
    EXTRACT_BITS("extractBits", 3),
    INSERT_BITS("insertBits", 4),
    REVERSE_BITS("reverseBits", 1);

    // temporarily commented out until implementation of vectors is concrete
    // SHIFT_LEFT("shiftLeft", 2),
    // SHIFT_RIGHT("shiftRight", 2);
}

internal enum class BuiltinLogicalExpr(override val operator: String, override val args: Int) : BuiltinExpr {
    ALL("all", 1),
    ANY("any", 1),
    SELECT("select", 3);
}