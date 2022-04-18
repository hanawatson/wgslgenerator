package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.BREAK
import wgslsmith.wgslgenerator.ast.statement.ContextSpecificStat.FALLTHROUGH
import wgslsmith.wgslgenerator.tables.SymbolTable

internal class ContextSpecificStatement : Statement() {
    private lateinit var keyword: String
    
    override lateinit var stat: Stat

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): Statement {
        this.stat = stat

        when (stat) {
            BREAK       -> keyword = "break"
            FALLTHROUGH -> keyword = "fallthrough"
        }

        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        return arrayListOf("$keyword;")
    }
}