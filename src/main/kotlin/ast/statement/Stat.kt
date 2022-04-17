package wgslsmith.wgslgenerator.ast.statement

internal interface Stat

internal interface AssignStat : Stat {
    val operator: String
}

internal enum class AssignEqStat(override val operator: String) : AssignStat {
    ASSIGN_SIMPLE("="),

    // temporarily commented due to lack of implementation in naga
    // ASSIGN_PHONY("=");
}

internal interface AssignCompoundStat : AssignStat

internal enum class AssignArithmeticCompoundStat(override val operator: String) : AssignCompoundStat {
    ADD("+="),
    DIV("/="),
    MINUS("-="),
    MOD("%="),
    MULT("*=");
}

internal enum class AssignBitCompoundStat(override val operator: String) : AssignCompoundStat {
    BIT_OR("|="),
    BIT_AND("&="),
    BIT_EXCLUSIVE_OR("^=");
}

internal enum class AssignIncDecCompoundStat(override val operator: String) : AssignCompoundStat {
    DECREMENT("--"),
    INCREMENT("++");
}

internal enum class AssignLogicalCompoundStat(override val operator: String) : AssignCompoundStat {
    OR("|="),
    AND("&=");
}

internal enum class ControlFlowStat : Stat {
    IF,
    SWITCH;
}

// temporary removal of compound assignments until probability is implemented to encourage generation of control flow
/*internal val assignStats = ArrayList<Stat>(
    AssignEqStat.values().asList() +
            AssignArithmeticCompoundStat.values().asList() +
            AssignIncDecCompoundStat.values().asList() +
            AssignBitCompoundStat.values().asList() +
            AssignLogicalCompoundStat.values().asList()
)*/

// holds statements that can only be used in certain contexts e.g. fallthrough in a switch case
internal enum class ContextSpecificStat : Stat {
    BREAK,
    FALLTHROUGH;
}

internal val assignStats = ArrayList<Stat>(AssignEqStat.values().asList())

// allStats excludes ContextSpecificStat members as these cannot be used normally!
internal val allStats = ArrayList<Stat>(assignStats + ControlFlowStat.values().asList())