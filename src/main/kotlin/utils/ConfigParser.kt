package wgslsmith.wgslgenerator.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
internal data class Config(
    val typeConfig: TypeConfig, val exprConfig: ExprConfig, val statConfig: StatConfig, val moduleConfig: ModuleConfig
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
    val replace_vector_binary_operand_with_other_type: Double,
    val replace_matrix_binary_operand_with_other_type: Double, val ratio_symbol_selection_to_zero_value: Double
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
    val max_statements_in_loop_body: Int, val max_statements_in_switch_body: Int, val max_if_else_branches: Int,
    val max_switch_cases: Int
)

@Serializable
internal data class StatChanceOptions(
    val generate_statement: Double, val generate_if_else_branch: Double, val generate_else_branch: Double,
    val generate_switch_case: Double, val generate_default_switch_case_before_last: Double,
    val generate_continuing_block: Double, val generate_continuing_break_if_statement: Double,
    val assign_expression_to_new_variable: Double, val omit_for_loop_initializer: Double,
    val omit_for_loop_condition: Double, val omit_for_loop_update: Double,
)

@Serializable
internal data class StatOptions(
    val prevent_code_after_control_flow_interruption: Boolean, val prevent_fallthrough_in_last_switch_case: Boolean,
    val ensure_no_duplicate_switch_cases: Boolean, val ensure_for_loop_termination: Boolean,
    val ensure_loop_termination: Boolean, val ensure_while_loop_termination: Boolean,
    val ensure_continue_is_valid: Boolean
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
    val loop_break: Double, val loop_continue: Double, val loop_return: Double, val switch_break: Double,
    val switch_fallthrough: Double
) : SubStatProbabilities

@Serializable
internal data class SubControlFlowStatProbabilities(
    val for_loop: Double, val if_else: Double, val loop: Double, val switch: Double, val while_loop: Double
) : SubStatProbabilities

@Serializable
internal data class ModuleConfig(
    val moduleBounds: ModuleBounds, val moduleChanceOptions: ModuleChanceOptions, val moduleOptions: ModuleOptions
)

@Serializable
internal data class ModuleBounds(
    val max_consts: Int, val max_globals: Int
)

@Serializable
internal data class ModuleChanceOptions(
    val generate_const: Double, val generate_global: Double
)

@Serializable
internal data class ModuleOptions(
    val use_output_buffer: Boolean
)

internal class ConfigParser(configFileContents: String) {
    init {
        val config = try {
            Json.decodeFromString<Config>(configFileContents)
        } catch (exception: Exception) {
            throw Exception("Invalid config file provided!")
        }
        CNFG.populateFromConfig(config)
    }
}