package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.SWITCH_BREAK
import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.SWITCH_FALLTHROUGH

internal class ContextSpecificStatement(override var stat: Stat) : Statement {
    private lateinit var keyword: String

    init {
        when (stat) {
            SWITCH_BREAK       -> keyword = "break"
            SWITCH_FALLTHROUGH -> keyword = "fallthrough"
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("$keyword;")
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return arrayListOf()
        }
    }
}