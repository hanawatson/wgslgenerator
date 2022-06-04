package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

class Shader {
    private var computeShaderStage: ComputeShaderStage

    init {
        val symbolTable = SymbolTable()

        var currentConsts = 0

        while (PRNG.eval(CNFG.generateConst) && currentConsts < CNFG.maxConsts) {
            ModuleScope.generateNewConst(symbolTable)
            currentConsts++
        }

        var currentGlobals = 0

        while (PRNG.eval(CNFG.generateGlobal) && currentGlobals < CNFG.maxGlobals) {
            ModuleScope.generateNewGlobal(symbolTable)
            currentGlobals++
        }

        computeShaderStage = ComputeShaderStage(symbolTable, ModuleScope.globals)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        if (CNFG.useOutputBuffer) {
            // insert fixed output buffer to hold checksum
            stringBuilder.append("struct outputBuffer {\n")
            stringBuilder.append("\toutput: u32,\n")
            stringBuilder.append("}\n\n")
            stringBuilder.append("@group(0) @binding(0)\n")
            stringBuilder.append("var<storage, read_write> checksum: outputBuffer;\n\n")
        }

        stringBuilder.append("$ModuleScope\n")

        // here we add globals/structs/functions. make sure to add extra lines between them

        stringBuilder.append(computeShaderStage.toString())
        return stringBuilder.toString()
    }
}