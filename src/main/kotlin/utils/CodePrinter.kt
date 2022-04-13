package wgslsmith.wgslgenerator.utils

internal class CodePrinter {
    private var lines = ArrayList<String>()

    fun append(line: String) {
        lines.add(line)
    }

    fun printCode(): String {
        val stringBuilder = StringBuilder()
        for (line in lines) {
            stringBuilder.append(line + "\n")
        }
        return stringBuilder.toString()
    }

    fun getTabbedLines(indentNumber: Int): ArrayList<String> {
        val tabbedLines = ArrayList<String>()
        for (line in lines) {
            var tabbedLine = line
            for (i in 1..indentNumber) {
                tabbedLine = "\t" + tabbedLine
            }
            tabbedLines.add(tabbedLine)
        }
        return tabbedLines
    }

    fun addLines(lines: ArrayList<String>) {
        this.lines.addAll(lines)
    }
}