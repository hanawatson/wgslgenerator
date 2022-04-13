package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable

class Shader {
    private var computeShaderStage: ComputeShaderStage? = null

    fun generate(): Shader {
        val symbolTable = SymbolTable()
        // globals/structs/functions generated here, globals should be added to table also
        computeShaderStage = ComputeShaderStage().generate(symbolTable)
        return this
    }

    override fun toString(): String {
        if (computeShaderStage == null) {
            return "Generation function not called on shader!"
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append("var<private> checksum: i32;\n\n")

        // here we add globals/structs/functions. make sure to add extra lines between them

        stringBuilder.append(computeShaderStage.toString())
        return stringBuilder.toString()
    }
}