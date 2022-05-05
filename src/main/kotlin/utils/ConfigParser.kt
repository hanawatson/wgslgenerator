package wgslsmith.wgslgenerator.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
internal data class Config(
    val typeConfig: TypeConfig, val exprConfig: ExprConfig, val statConfig: StatConfig
)

@Serializable
internal data class TypeConfig(
    val typeBounds: TypeBounds, val typeChanceOptions: TypeChanceOptions, val typeProbabilities: TypeProbabilities
)

@Serializable
internal data class TypeBounds(
    val max_array_element_count: Int = 5, val max_array_nest_depth: Int = 3
)

@Serializable
internal data class TypeChanceOptions(
    val omit_type_from_declaration: Double, val omit_type_from_composite_construction: Double,
    val use_hexadecimal_numeric_literal: Double, val use_suffix_with_numeric_literal: Double,
    val construct_vector_with_single_value: Double
)

@Serializable
internal data class TypeProbabilities(
    val bool: Double, val float32: Double, val int32: Double, val uint32: Double,
    val array: Double, val matrix: Double, val vector: Double
)

@Serializable
internal data class ExprConfig(
    val exprBounds: ExprBounds, val exprChanceOptions: ExprChanceOptions,
    val exprOptions: ExprOptions, val exprProbabilities: ExprProbabilities
)

@Serializable
internal data class ExprBounds(
    val max_expression_nest_depth: Int, val max_subscript_access_expression_nest_depth: Int,
    val max_excess_expression_parentheses: Int
)

@Serializable
internal data class ExprChanceOptions(
    val generate_simple_subscript_access: Double, val generate_parentheses_around_expression: Double,
    val replace_vector_non_mult_operand_with_scalar: Double, val replace_vector_mult_operand_with_other: Double,
    val replace_matrix_mult_operand_with_other: Double, val ratio_symbol_selection_to_zero_value: Double
)

@Serializable
internal data class ExprOptions(
    val use_necessary_expression_parentheses: Boolean, val use_useful_expression_parentheses: Boolean,
    val use_excess_expression_parentheses: Boolean, val ensure_complex_subscript_access_in_bounds: Boolean
)

@Serializable
internal data class ExprProbabilities(
    val binary_operation: Double, val unary_operation: Double, val comparison: Double, val identity: Double,
    val type_conversion: Double, val builtin_function: Double, val data_pack_unpack: Double,
    val swizzle_subscript_access: Double
)

@Serializable
internal data class StatConfig(
    val statBounds: StatBounds, val statChanceOptions: StatChanceOptions, val statOptions: StatOptions,
    val statProbabilities: StatProbabilities
)

@Serializable
internal data class StatBounds(
    val max_statement_nest_depth: Int, val max_statements_in_body: Int, val max_statements_in_if_body: Int,
    val max_statements_in_switch_body: Int, val max_if_else_branches: Int, val max_switch_cases: Int
)

@Serializable
internal data class StatChanceOptions(
    val generate_statement: Double, val generate_if_else_branch: Double, val generate_else_branch: Double,
    val generate_switch_case: Double, val generate_default_switch_case_before_last: Double,
    val assign_expression_to_new_variable: Double
)

@Serializable
internal data class StatOptions(
    val prevent_code_after_break_statement: Boolean, val prevent_fallthrough_in_last_switch_case: Boolean,
    val ensure_no_duplicate_switch_cases: Boolean
)

@Serializable
internal data class StatProbabilities(
    val assignment: Double, val sub_assignment: SubAssignmentStatProbabilities,
    val context_specific: Double, val sub_context_specific: SubContextSpecificStatProbabilities,
    val control_flow: Double, val sub_control_flow: SubControlFlowStatProbabilities
)

internal interface SubStatProbabilities

@Serializable
internal data class SubAssignmentStatProbabilities(
    val compound_assignment: Double, val declaration: Double, val decrement: Double, val increment: Double,
    val phony_assignment: Double, val simple_assignment: Double
) : SubStatProbabilities

@Serializable
internal data class SubContextSpecificStatProbabilities(
    val switch_break: Double, val switch_fallthrough: Double
) : SubStatProbabilities

@Serializable
internal data class SubControlFlowStatProbabilities(
    val if_else: Double, val switch: Double
) : SubStatProbabilities

internal class ConfigParser(configPath: String) {
    // default config path
    constructor() : this("src/main/resources/tintAndNagaConfig.json")

    init {
        val configString = try {
            File(configPath).readText()
        } catch (exception: Exception) {
            throw Exception("Invalid file path provided!")
        }
        val config = try {
            Json.decodeFromString<Config>(configString)
        } catch (exception: Exception) {
            throw Exception("Invalid config file provided!")
        }
        CNFG.populateFromConfig(config)
    }
}