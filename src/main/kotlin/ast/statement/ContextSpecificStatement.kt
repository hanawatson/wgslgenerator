package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.WGSLType

internal class ContextSpecificStatement(override var stat: Stat) : Statement {
    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("${(stat as ContextSpecificStat).keyword};")
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            return arrayListOf()
        }
    }
}