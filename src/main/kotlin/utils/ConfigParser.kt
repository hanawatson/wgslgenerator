package wgslsmith.wgslgenerator.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(
    val typeConfig: TypeConfig, val exprConfig: ExprConfig, val statConfig: StatConfig
)

@Serializable
data class TypeConfig(
    val typeBounds: TypeBounds, val typeChanceOptions: TypeChanceOptions, val typeProbabilities: TypeProbabilities
)

@Serializable
data class TypeBounds(
    val max_array_element_count: Int = 5, val max_array_nest_depth: Int = 3
)

@Serializable
data class TypeChanceOptions(
    val omit_type_from_declaration: Double, val omit_type_from_composite_construction: Double,
    val use_hexadecimal_numeric_literal: Double, val use_suffix_with_numeric_literal: Double,
    val construct_vector_with_single_value: Double
)

@Serializable
data class TypeProbabilities(
    val bool: Double, val float32: Double, val int32: Double, val uint32: Double,
    val array: Double, val matrix: Double, val vector: Double
)

@Serializable
data class ExprConfig(
    val exprBounds: ExprBounds, val exprChanceOptions: ExprChanceOptions,
    val exprOptions: ExprOptions, val exprProbabilities: ExprProbabilities
)

@Serializable
data class ExprBounds(
    val max_expression_nest_depth: Int, val max_subscript_access_expression_nest_depth: Int,
    val max_excess_expression_parentheses: Int
)

@Serializable
data class ExprChanceOptions(
    val generate_parentheses_around_expression: Double, val replace_vector_non_mult_operand_with_scalar: Double,
    val replace_vector_mult_operand_with_other: Double, val replace_matrix_mult_operand_with_other: Double
)

@Serializable
data class ExprOptions(
    val use_necessary_expression_parentheses: Boolean, val use_useful_expression_parentheses: Boolean,
    val use_excess_expression_parentheses: Boolean, val ensure_subscript_access_in_bounds: Boolean
)

@Serializable
data class ExprProbabilities(
    val binaryOperation: Double, val unaryOperation: Double, val comparison: Double, val identity: Double,
    val typeConversion: Double, val builtinFunction: Double, val dataPackUnpack: Double,
    val swizzleSubscriptAccess: Double
)

@Serializable
data class StatConfig(
    val statBounds: StatBounds, val statChanceOptions: StatChanceOptions, val statOptions: StatOptions
)

@Serializable
data class StatBounds(
    val max_statement_nest_depth: Int, val max_statements_in_body: Int, val max_statements_in_if_body: Int,
    val max_statements_in_switch_body: Int, val max_if_else_branches: Int, val max_switch_cases: Int
)

@Serializable
data class StatChanceOptions(
    val generate_statement: Double, val generate_if_else_branch: Double, val generate_else_branch: Double,
    val generate_switch_case: Double, val generate_default_switch_case_before_last: Double,
    val assign_expression_to_new_variable: Double
)

@Serializable
data class StatOptions(
    val prevent_code_after_break_statement: Boolean, val prevent_fallthrough_in_last_switch_case: Boolean,
    val ensure_no_duplicate_switch_cases: Boolean
)

internal class ConfigParser(configPath: String) {
    // default config path
    constructor() : this("src/main/resources/defaultConfig.json")

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