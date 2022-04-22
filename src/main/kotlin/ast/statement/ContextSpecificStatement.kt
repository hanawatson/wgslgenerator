package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.BREAK
import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.FALLTHROUGH

internal class ContextSpecificStatement(override var stat: Stat) : Statement {
    private lateinit var keyword: String

    init {
        when (stat) {
            BREAK       -> keyword = "break"
            FALLTHROUGH -> keyword = "fallthrough"
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("$keyword;")
    }
}