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

internal enum class AssignCompoundStat(override val operator: String) : AssignStat {
    BINARY_OPERATOR(""),
    DECREMENT("--"),
    INCREMENT("++");
}

internal enum class ControlFlowStat : Stat {
    IF,
    SWITCH;
}

// holds statements that can only be used in certain contexts e.g. fallthrough in a switch case
internal enum class ContextSpecificStat : Stat {
    BREAK,
    FALLTHROUGH;
}

internal val assignStats = ArrayList<Stat>(AssignCompoundStat.values().asList() + AssignEqStat.values().asList())

// allStats excludes ContextSpecificStat members as these cannot be used normally!
internal val allStats = ArrayList<Stat>(assignStats + ControlFlowStat.values().asList())