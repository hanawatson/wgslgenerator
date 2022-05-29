package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

internal class ComputeShaderStage(symbolTable: SymbolTable) {
    private var body: ScopeBody

    init {
        if (CNFG.prob(scalarUnIntType) > 0.0) {
            symbolTable.addNewNonWriteableSymbol("local_index", scalarUnIntType)
        }
        if (CNFG.prob(vector3UnIntType) > 0.0) {
            symbolTable.addNewNonWriteableSymbol("global_id", vector3UnIntType)
            symbolTable.addNewNonWriteableSymbol("local_id", vector3UnIntType)
            symbolTable.addNewNonWriteableSymbol("workgroup_id", vector3UnIntType)
            symbolTable.addNewNonWriteableSymbol("num_workgroups", vector3UnIntType)
        }
        body = ScopeBody(symbolTable, ScopeState.NONE, 0)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("@stage(compute) @workgroup_size(1)\n")

        // defined as "main" to ensure compatibility with wgslsmith harness
        stringBuilder.append("fn main(\n")

        if (CNFG.prob(scalarUnIntType) > 0.0) {
            stringBuilder.append("\t@builtin(local_invocation_index) local_index: u32,\n")
        }
        if (CNFG.prob(vector3UnIntType) > 0.0) {
            stringBuilder.append("\t@builtin(global_invocation_id) global_id: vec3<u32>,\n")
            stringBuilder.append("\t@builtin(local_invocation_id) local_id: vec3<u32>,\n")
            stringBuilder.append("\t@builtin(workgroup_id) workgroup_id: vec3<u32>,\n")
            stringBuilder.append("\t@builtin(num_workgroups) num_workgroups: vec3<u32>\n")
        }
        stringBuilder.append(") {\n")

        for (bodyLine in body.getTabbedLines()) {
            stringBuilder.append(bodyLine + "\n")
        }

        // calculate checksum of globals - currently unimplemented and simply returns as 0
        stringBuilder.append("\tchecksum.output = 0;\n")

        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}