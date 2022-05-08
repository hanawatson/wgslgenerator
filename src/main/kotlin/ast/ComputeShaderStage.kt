package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

internal class ComputeShaderStage {
    private lateinit var body: ScopeBody

    fun generate(symbolTable: SymbolTable): ComputeShaderStage {
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
        return this
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        // temporary workaround for Tint and naga recognising opposite ways of declaring compute stage
        stringBuilder.append("@COMPUTE_STAGE @workgroup_size(1)\n")
        stringBuilder.append("fn compute_main(\n")
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
        stringBuilder.append("\tchecksum = 0;\n")

        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}