package wgslsmith.wgslgenerator.ast.statement

internal interface Stat

internal interface AssignmentStat : Stat {
    val operator: String
}

internal enum class AssignmentCompoundStat(override val operator: String) : AssignmentStat {
    BINARY_OPERATOR(""),
    DECREMENT("--"),
    INCREMENT("++");
}

internal enum class AssignmentEqStat(override val operator: String = "=") : AssignmentStat {
    ASSIGN_DECLARE,
    ASSIGN_LET,
    ASSIGN_PHONY,
    ASSIGN_SIMPLE;
}

internal enum class ControlFlowStat : Stat {
    FOR,
    IF,
    LOOP,
    SWITCH,
    WHILE;
}

// holds statements that can only be used in certain contexts e.g. fallthrough in a switch case
internal enum class ContextSpecificStat(val keyword: String) : Stat {
    LOOP_BREAK("break"),
    LOOP_CONTINUE("continue"),
    LOOP_RETURN("return"),
    SWITCH_BREAK("break"),
    SWITCH_FALLTHROUGH("fallthrough");
}

internal val assignStats =
    ArrayList<Stat>(AssignmentCompoundStat.values().asList() + AssignmentEqStat.values().asList())

// allStats excludes ContextSpecificStat members as these cannot be used normally!
internal val allStats = ArrayList<Stat>(assignStats + ControlFlowStat.values().asList())