package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

class Shader {
    private val moduleScope: ModuleScope
    private val computeShaderStage: ComputeShaderStage

    init {
        val symbolTable = SymbolTable()
        moduleScope = ModuleScope()

        var currentConsts = 0
        while (PRNG.eval(CNFG.generateConst) && currentConsts < CNFG.maxConsts) {
            moduleScope.generateNewConst(symbolTable)
            currentConsts++
        }

        var currentGlobals = 0
        while (PRNG.eval(CNFG.generateGlobal) && currentGlobals < CNFG.maxGlobals) {
            moduleScope.generateNewGlobal(symbolTable)
            currentGlobals++
        }

        var currentFunctions = 0
        while (PRNG.eval(CNFG.generateFunction) && currentFunctions < CNFG.maxFunctions) {
            moduleScope.generateNewFunction(symbolTable)
            currentFunctions++
        }

        computeShaderStage = ComputeShaderStage(symbolTable, moduleScope.globals)
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

        stringBuilder.append("$moduleScope\n")

        stringBuilder.append(computeShaderStage.toString())
        return stringBuilder.toString()
    }
}