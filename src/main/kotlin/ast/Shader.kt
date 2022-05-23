package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable

class Shader {
    private var computeShaderStage: ComputeShaderStage

    init {
        val symbolTable = SymbolTable()
        // globals/structs/functions generated here, globals should be added to table also
        computeShaderStage = ComputeShaderStage(symbolTable)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        // insert fixed output buffer to hold checksum
        stringBuilder.append("struct outputBuffer {\n")
        stringBuilder.append("\toutput: i32,\n")
        stringBuilder.append("}\n\n")
        stringBuilder.append("@group(0) @binding(0)\n")
        stringBuilder.append("var<storage, read_write> checksum: outputBuffer;\n\n")

        // here we add globals/structs/functions. make sure to add extra lines between them

        stringBuilder.append(computeShaderStage.toString())
        return stringBuilder.toString()
    }
}