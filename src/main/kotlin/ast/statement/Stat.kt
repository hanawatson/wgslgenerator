package wgslsmith.wgslgenerator.ast.statement

internal interface Stat

internal interface AssignmentStat : Stat {
    val operator: String
}

internal enum class AssignmentEqStat(override val operator: String = "=") : AssignmentStat {
    ASSIGN_DECLARE,
    ASSIGN_LET,

    // temporarily commented due to lack of implementation in naga
    ASSIGN_PHONY,

    ASSIGN_SIMPLE;
}

internal enum class AssignmentCompoundStat(override val operator: String) : AssignmentStat {
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
    SWITCH_BREAK,
    SWITCH_FALLTHROUGH;
}

// temporary disabling of DECREMENT, INCREMENT due to nonfunctional implementation with subscripts/swizzles in naga
internal val assignStats =
    ArrayList<Stat>(/*AssignmentCompoundStat.values().asList()*/
        arrayListOf(AssignmentCompoundStat.BINARY_OPERATOR) + AssignmentEqStat.values().asList()
    )

// allStats excludes ContextSpecificStat members as these cannot be used normally!
internal val allStats = ArrayList<Stat>(assignStats + ControlFlowStat.values().asList())