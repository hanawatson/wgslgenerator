package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable

class ComputeShaderStage {
    private lateinit var body: ScopeBody

    fun generate(symbolTable: SymbolTable): ComputeShaderStage {
        // add 5 builtin constants below once types are viable to symbol table
        body = ScopeBody(ScopeState.NONE).generate(symbolTable, 0)
        return this
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("@stage(compute) @workgroup_size(1)\n")
        stringBuilder.append("fn compute_main(\n")
        //stringBuilder.append("\t@builtin(local_invocation_index) local_index: u32,\n")
        //stringBuilder.append("\t@builtin(global_invocation_id) global_id: vec3<u32>,\n")
        //stringBuilder.append("\t@builtin(local_invocation_id) local_id: vec3<u32>,\n")
        //stringBuilder.append("\t@builtin(workgroup_id) workgroup_id: vec3<u32>,\n")
        //stringBuilder.append("\t@builtin(num_workgroups) num_workgroups: vec3<u32>,\n")
        stringBuilder.append(") {\n")

        for (bodyLine in body.getTabbedLines()) {
            stringBuilder.append(bodyLine + "\n")
        }

        // calculate checksum of globals
        stringBuilder.append("\tchecksum = 0;\n")

        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}