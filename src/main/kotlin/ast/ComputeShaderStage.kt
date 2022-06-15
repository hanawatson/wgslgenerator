package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG

internal class ComputeShaderStage(symbolTable: SymbolTable, private val globals: ArrayList<Symbol>) {
    private var body: ScopeBody

    init {
        if (CNFG.prob(scalarUnIntType) > 0.0) {
            symbolTable.addNewNonWriteableSymbol(Symbol("local_index", scalarUnIntType))
        }
        if (CNFG.prob(vector3UnIntType) > 0.0) {
            symbolTable.addNewNonWriteableSymbol(Symbol("global_id", vector3UnIntType))
            symbolTable.addNewNonWriteableSymbol(Symbol("local_id", vector3UnIntType))
            symbolTable.addNewNonWriteableSymbol(Symbol("workgroup_id", vector3UnIntType))
            symbolTable.addNewNonWriteableSymbol(Symbol("num_workgroups", vector3UnIntType))
        }
        body = ScopeBody(symbolTable, ScopeState.NONE, 0)
    }

    private fun getChecksumComponents(global: Symbol): ArrayList<String> {
        val checksumComponents = ArrayList<String>()

        when (val type = global.type) {
            is WGSLScalarType -> {
                checksumComponents.add(global.name)
            }
            is WGSLVectorType -> {
                for (i in 0 until type.length) {
                    val component = Symbol("${global.name}[$i]", type.componentType)
                    checksumComponents.addAll(getChecksumComponents(component))
                }
            }
            is WGSLMatrixType -> {
                val columnType = WGSLVectorType(type.componentType, type.length)
                for (i in 0 until type.width) {
                    val column = Symbol("${global.name}[$i]", columnType)
                    checksumComponents.addAll(getChecksumComponents(column))
                }
            }
            is WGSLArrayType  -> {
                for (i in 0 until type.elementCountValue) {
                    val element = Symbol("${global.name}[$i]", type.elementType)
                    checksumComponents.addAll(getChecksumComponents(element))
                }
            }
            else              -> throw Exception("Attempt to generate checksum for unknown type ${global.type}!")
        }

        return checksumComponents
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("// Main function\n")
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
            stringBuilder.append("$bodyLine\n")
        }

        if (CNFG.useOutputBuffer) {
            // calculate checksum of globals
            val checksumComponents = ArrayList<String>()

            for (global in globals) {
                checksumComponents.addAll(getChecksumComponents(global))
            }

            stringBuilder.append("\n\t// Checksum calculation\n")
            stringBuilder.append("\tchecksum.output = 0u;\n")

            // use a sum method to calculate the checksum due to low cost and lack of WGSL hash functions
            for (checksumComponent in checksumComponents) {
                stringBuilder.append("\tchecksum.output += u32($checksumComponent);\n")
            }
        }

        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}