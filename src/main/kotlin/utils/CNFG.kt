package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.ast.statement.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

// ConfigurationManager
internal object CNFG {
    private val bounds = ArrayList<Int>()
    private val chanceOptions = ArrayList<Double>()
    private val options = ArrayList<Boolean>()

    private var typeBoundsStartIndex = 0
    val maxArrayElementCount: Int by lazy { bounds[typeBoundsStartIndex] }
    val maxArrayNestDepth: Int by lazy { bounds[typeBoundsStartIndex + 1] }

    private var exprBoundsStartIndex = 0
    val maxExcessExpressionParentheses: Int by lazy { bounds[exprBoundsStartIndex] }
    val maxExpressionNestDepth: Int by lazy { bounds[exprBoundsStartIndex + 1] }
    val maxSubscriptAccessExpressionNestDepth: Int by lazy { bounds[exprBoundsStartIndex + 2] }

    private var statBoundsStartIndex = 0
    val maxIfElseBranches: Int by lazy { bounds[statBoundsStartIndex] }
    val maxStatementNestDepth: Int by lazy { bounds[statBoundsStartIndex + 1] }
    val maxStatementsInBody: Int by lazy { bounds[statBoundsStartIndex + 2] }
    val maxStatementsInIfBody: Int by lazy { bounds[statBoundsStartIndex + 3] }
    val maxStatementsInLoopBody: Int by lazy { bounds[statBoundsStartIndex + 4] }
    val maxStatementsInSwitchBody: Int by lazy { bounds[statBoundsStartIndex + 5] }
    val maxSwitchCases: Int by lazy { bounds[statBoundsStartIndex + 6] }

    private var moduleBoundsStartIndex = 0
    val maxConsts: Int by lazy { bounds[moduleBoundsStartIndex] }

    private var typeChanceOptionsStartIndex = 0
    val constructVectorWithSingleValue: Double by lazy { chanceOptions[typeChanceOptionsStartIndex] }
    val omitTypeFromDeclaration: Double by lazy { chanceOptions[typeChanceOptionsStartIndex + 1] }
    private val omitTypeFromCompositeConstructionUserdef: Double by lazy {
        chanceOptions[typeChanceOptionsStartIndex + 2]
    }
    var omitTypeFromCompositeConstruction = 0.0
    val useHexadecimalNumericLiteral: Double by lazy { chanceOptions[typeChanceOptionsStartIndex + 3] }
    private val useSuffixWithNumericLiteralUserdef: Double by lazy { chanceOptions[typeChanceOptionsStartIndex + 4] }
    var useSuffixWithNumericLiteral = 0.0

    private var exprChanceOptionsStartIndex = 0
    private val generateSimpleSubscriptAccessUserdef: Double by lazy { chanceOptions[exprChanceOptionsStartIndex] }
    var generateSimpleSubscriptAccess = 0.0
    val generateParenthesesAroundExpression: Double by lazy { chanceOptions[exprChanceOptionsStartIndex + 1] }
    private val replaceMatrixBinaryOperandWithOtherTypeUserdef: Double by lazy {
        chanceOptions[exprChanceOptionsStartIndex + 2]
    }
    var replaceMatrixBinaryOperandWithOtherType = 0.0
    private val replaceVectorBinaryOperandWithOtherTypeUserdef: Double by lazy {
        chanceOptions[exprChanceOptionsStartIndex + 3]
    }
    var replaceVectorBinaryOperandWithOtherType = 0.0
    private val ratioSymbolSelectionToZeroValue: Double by lazy { chanceOptions[exprChanceOptionsStartIndex + 4] }

    private var statChanceOptionsStartIndex = 0
    val assignExpressionToNewVariable: Double by lazy { chanceOptions[statChanceOptionsStartIndex] }
    val generateContinuingBlock: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 1] }
    val generateContinuingBreakIfStatement: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 2] }
    val generateDefaultSwitchCaseBeforeLast: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 3] }
    val generateElseBranch: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 4] }
    val generateIfElseBranch: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 5] }
    val generateStatement: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 6] }
    val generateSwitchCase: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 7] }
    val omitForLoopCondition: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 8] }
    val omitForLoopInitializer: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 9] }
    val omitForLoopUpdate: Double by lazy { chanceOptions[statChanceOptionsStartIndex + 10] }

    private var moduleChanceOptionsStartIndex = 0
    val declareConstWithLet: Double by lazy { chanceOptions[moduleChanceOptionsStartIndex] }
    val generateConst: Double by lazy { chanceOptions[moduleChanceOptionsStartIndex + 1] }

    private var exprOptionsStartIndex = 0
    val ensureComplexSubscriptAccessInBounds: Boolean by lazy { options[exprOptionsStartIndex] }
    val useExcessExpressionParentheses: Boolean by lazy { options[exprOptionsStartIndex + 1] }
    val useNecessaryExpressionParentheses: Boolean by lazy { options[exprOptionsStartIndex + 2] }
    val useUsefulExpressionParentheses: Boolean by lazy { options[exprOptionsStartIndex + 3] }

    private var statOptionsStartIndex = 0
    val ensureContinueIsValid: Boolean by lazy { options[statOptionsStartIndex] }
    val ensureForLoopTermination: Boolean by lazy { options[statOptionsStartIndex + 1] }
    val ensureLoopTermination: Boolean by lazy { options[statOptionsStartIndex + 2] }
    val ensureWhileLoopTermination: Boolean by lazy { options[statOptionsStartIndex + 3] }
    val ensureNoDuplicateSwitchCases: Boolean by lazy { options[statOptionsStartIndex + 4] }
    val preventCodeAfterControlFlowInterruption: Boolean by lazy { options[statOptionsStartIndex + 5] }
    val preventFallthroughInLastSwitchCase: Boolean by lazy { options[statOptionsStartIndex + 6] }

    private var moduleOptionsStartIndex = 0
    val useOutputBuffer: Boolean by lazy { options[moduleOptionsStartIndex] }

    private val typeProbMap = HashMap<WGSLType, Double>()
    private val scalarProbTotal: Double by lazy {
        scalarTypes.fold(0.0) { acc, type -> acc + typeProbMap[type]!! }
    }
    private val matCompProbTotal: Double by lazy {
        matrixComponentTypes.fold(0.0) { acc, type -> acc + typeProbMap[type]!! }
    }
    private val arrElemProbTotal: Double by lazy {
        arrayElementTypes.fold(0.0) { acc, type -> acc + typeProbMap[type]!! }
    }
    private val typeParameterMap: HashMap<String, WGSLType> by lazy {
        hashMapOf(
            Pair("bool", scalarBoolType),
            Pair("float32", scalarFloatType),
            Pair("int32", scalarIntType),
            Pair("uint32", scalarUnIntType),
            Pair("array", abstractWGSLArrayType),
            Pair("matrix", abstractWGSLMatrixType),
            Pair("vector", abstractWGSLVectorType)
        )
    }

    private val expressionProbMap = HashMap<ExpressionCompanion, Double>()
    private val expressionParameterMap: HashMap<String, ExpressionCompanion> by lazy {
        hashMapOf(
            Pair("swizzle_subscript_access", AccessExpression),
            Pair("binary_operation", BinaryExpression),
            Pair("builtin_function", BuiltinExpression),
            Pair("comparison", ComparisonExpression),
            Pair("type_conversion", ConversionExpression),
            Pair("data_pack_unpack", DataExpression),
            Pair("identity", IdentityExpression),
            Pair("unary_operation", UnaryExpression)
        )
    }
    private val exprProbMap = HashMap<Expr, Double>()

    private val statementProbMap = HashMap<StatementCompanion, Double>()
    private val statementParameterMap: HashMap<String, StatementCompanion> by lazy {
        hashMapOf(
            Pair("assignment", AssignmentStatement),
            Pair("context_specific", ContextSpecificStatement),
            Pair("control_flow", ControlFlowStatement)
        )
    }
    private val subStatSizeMap: HashMap<StatementCompanion, Int> by lazy {
        hashMapOf(
            Pair(AssignmentStatement, SubAssignmentStatProbabilities::class.memberProperties.size),
            Pair(ContextSpecificStatement, SubContextSpecificStatProbabilities::class.memberProperties.size),
            Pair(ControlFlowStatement, SubControlFlowStatProbabilities::class.memberProperties.size)
        )
    }
    private val subStatProbMap = HashMap<Stat, Double>()
    private val subStatParameterMap: HashMap<String, Stat> by lazy {
        hashMapOf(
            Pair("compound_assignment", AssignmentCompoundStat.BINARY_OPERATOR),
            Pair("declaration", AssignmentEqStat.ASSIGN_DECLARE),
            Pair("decrement", AssignmentCompoundStat.DECREMENT),
            Pair("increment", AssignmentCompoundStat.INCREMENT),
            Pair("phony_assignment", AssignmentEqStat.ASSIGN_PHONY),
            Pair("simple_assignment", AssignmentEqStat.ASSIGN_SIMPLE),
            Pair("loop_break", ContextSpecificStat.LOOP_BREAK),
            Pair("loop_continue", ContextSpecificStat.LOOP_CONTINUE),
            Pair("loop_return", ContextSpecificStat.LOOP_RETURN),
            Pair("switch_break", ContextSpecificStat.SWITCH_BREAK),
            Pair("switch_fallthrough", ContextSpecificStat.SWITCH_FALLTHROUGH),
            Pair("for_loop", ControlFlowStat.FOR),
            Pair("if_else", ControlFlowStat.IF),
            Pair("loop", ControlFlowStat.LOOP),
            Pair("switch", ControlFlowStat.SWITCH),
            Pair("while_loop", ControlFlowStat.WHILE)
        )
    }
    private val statProbMap = HashMap<Stat, Double>()

    var tintSafe = true
    var nagaSafe = true

    private inline fun <reified T : Any> getParameterValues(
        propertiedClass: KClass<T>,
        classInstance: T
    ): ArrayList<Any?> {
        val parameters = ArrayList<Any?>()
        for (property in propertiedClass.memberProperties) {
            parameters.add(property.get(classInstance))
        }
        return parameters
    }

    fun populateFromConfig(config: Config) {
        val typeBoundsParameters = getParameterValues(TypeBounds::class, config.typeConfig.typeBounds)
        exprBoundsStartIndex = typeBoundsStartIndex + typeBoundsParameters.size
        val exprBoundsParameters = getParameterValues(ExprBounds::class, config.exprConfig.exprBounds)
        statBoundsStartIndex = exprBoundsStartIndex + exprBoundsParameters.size
        val statBoundsParameters = getParameterValues(StatBounds::class, config.statConfig.statBounds)
        moduleBoundsStartIndex = statBoundsStartIndex + statBoundsParameters.size
        val moduleBoundsParameters = getParameterValues(ModuleBounds::class, config.moduleConfig.moduleBounds)
        (typeBoundsParameters + exprBoundsParameters + statBoundsParameters + moduleBoundsParameters).forEach {
            bounds.add(it as Int)
        }

        val typeChanceOptionsParameters =
            getParameterValues(TypeChanceOptions::class, config.typeConfig.typeChanceOptions)
        exprChanceOptionsStartIndex = typeChanceOptionsStartIndex + typeChanceOptionsParameters.size
        val exprChanceOptionsParameters =
            getParameterValues(ExprChanceOptions::class, config.exprConfig.exprChanceOptions)
        statChanceOptionsStartIndex = exprChanceOptionsStartIndex + exprChanceOptionsParameters.size
        val statChanceOptionsParameters =
            getParameterValues(StatChanceOptions::class, config.statConfig.statChanceOptions)
        moduleChanceOptionsStartIndex = statChanceOptionsStartIndex + statChanceOptionsParameters.size
        val moduleChanceOptionsParameters =
            getParameterValues(ModuleChanceOptions::class, config.moduleConfig.moduleChanceOptions)
        (typeChanceOptionsParameters + exprChanceOptionsParameters + statChanceOptionsParameters + moduleChanceOptionsParameters).forEach {
            chanceOptions.add(it as Double)
        }

        val exprOptionsParameters = getParameterValues(ExprOptions::class, config.exprConfig.exprOptions)
        statOptionsStartIndex = exprOptionsStartIndex + exprOptionsParameters.size
        val statOptionsParameters = getParameterValues(StatOptions::class, config.statConfig.statOptions)
        moduleOptionsStartIndex = statOptionsStartIndex + statOptionsParameters.size
        val moduleOptionsParameters = getParameterValues(ModuleOptions::class, config.moduleConfig.moduleOptions)
        (exprOptionsParameters + statOptionsParameters + moduleOptionsParameters).forEach {
            options.add(it as Boolean)
        }

        // disable/enable certain features that are unsupported in Tint or naga
        omitTypeFromCompositeConstruction = if (nagaSafe) 0.0 else omitTypeFromCompositeConstructionUserdef
        generateSimpleSubscriptAccess = if (nagaSafe) 1.0 else generateSimpleSubscriptAccessUserdef
        useSuffixWithNumericLiteral = if (tintSafe) 1.0 else useSuffixWithNumericLiteralUserdef

        for (parameter in TypeProbabilities::class.memberProperties) {
            val parameterValue = parameter.get(config.typeConfig.typeProbabilities) as Double
            if (parameterValue < 0.0 || !parameterValue.isFinite()) {
                throw Exception("Invalid value passed in config file for parameter ${parameter.name}!")
            }
            typeProbMap[typeParameterMap[parameter.name]!!] = parameterValue
        }
        if (scalarProbTotal == 0.0) {
            throw Exception("Scalar types may not be completely disabled!")
        }
        if (matCompProbTotal == 0.0) {
            typeProbMap[abstractWGSLMatrixType] = 0.0
        }

        for (parameter in ExprProbabilities::class.memberProperties) {
            val parameterValue = parameter.get(config.exprConfig.exprProbabilities) as Double
            if (parameterValue < 0.0 || !parameterValue.isFinite()) {
                throw Exception("Invalid value passed in config file for parameter ${parameter.name}!")
            }
            expressionProbMap[expressionParameterMap[parameter.name]!!] = parameterValue
        }
        if (expressionProbMap[IdentityExpression] == 0.0) {
            throw Exception("Identity expressions may not be completely disabled!")
        }

        for (parameter in StatProbabilities::class.memberProperties) {
            val parameterValue = parameter.get(config.statConfig.statProbabilities)
            if (parameterValue is Double) {
                if (parameterValue < 0.0 || !parameterValue.isFinite()) {
                    throw Exception("Invalid value passed in config file for parameter ${parameter.name}!")
                }
                statementProbMap[statementParameterMap[parameter.name]!!] = parameterValue
            } else if (parameterValue is SubStatProbabilities) {
                when (parameterValue) {
                    is SubAssignmentStatProbabilities      -> {
                        for (subParameter in SubAssignmentStatProbabilities::class.memberProperties) {
                            val subParameterValue = subParameter.get(
                                config.statConfig.statProbabilities.sub_assignment
                            )
                            if (subParameterValue is Double) {
                                if (subParameterValue < 0.0 || !subParameterValue.isFinite()) {
                                    throw Exception("Invalid value passed in config file for parameter ${subParameter.name}!")
                                }
                                // remove "sub_" prefix for sub stat size retrieval
                                val parentStatement = statementParameterMap[parameter.name.removeRange(0..3)]!!
                                subStatProbMap[subStatParameterMap[subParameter.name]!!] =
                                    subParameterValue / subStatSizeMap[parentStatement]!!

                                // disable inc/dec due to nonfunctional implementation in Tint when
                                // acting on subscript/convenience accesses
                                if (tintSafe) {
                                    if (subParameter.name == "increment" || subParameter.name == "decrement") {
                                        subStatProbMap[subStatParameterMap[subParameter.name]!!] = 0.0
                                    }
                                }
                            }
                        }
                    }
                    is SubContextSpecificStatProbabilities -> {
                        for (subParameter in SubContextSpecificStatProbabilities::class.memberProperties) {
                            val subParameterValue = subParameter.get(
                                config.statConfig.statProbabilities.sub_context_specific
                            )
                            if (subParameterValue is Double) {
                                if (subParameterValue < 0.0 || !subParameterValue.isFinite()) {
                                    throw Exception("Invalid value passed in config file for parameter ${subParameter.name}!")
                                }
                                // remove "sub_" prefix for sub stat size retrieval
                                val parentStatement = statementParameterMap[parameter.name.removeRange(0..3)]!!
                                subStatProbMap[subStatParameterMap[subParameter.name]!!] =
                                    subParameterValue / subStatSizeMap[parentStatement]!!
                            }
                        }
                    }
                    is SubControlFlowStatProbabilities     -> {
                        for (subParameter in SubControlFlowStatProbabilities::class.memberProperties) {
                            var subParameterValue = subParameter.get(
                                config.statConfig.statProbabilities.sub_control_flow
                            )
                            if (subParameterValue is Double) {
                                if (subParameterValue < 0.0 || !subParameterValue.isFinite()) {
                                    throw Exception("Invalid value passed in config file for parameter ${subParameter.name}!")
                                }

                                val subStat = subStatParameterMap[subParameter.name]!!

                                when (subStat) {
                                    ControlFlowStat.LOOP,
                                    ControlFlowStat.WHILE -> {
                                        if (ensureLoopTermination) {
                                            // ensure loop termination can be achieved within specified prob parameters
                                            val requisitesProb = prob(AssignmentEqStat.ASSIGN_SIMPLE) *
                                                    prob(scalarBoolType) * prob(scalarIntType) *
                                                    prob(ContextSpecificStat.LOOP_BREAK) * prob(AssignmentCompoundStat.INCREMENT)
                                            val requisiteComparisonProb = if (subStat == ControlFlowStat.LOOP) {
                                                prob(ComparisonThExpr.MORE_THAN_OR_EQUAL)
                                            } else {
                                                prob(ComparisonThExpr.LESS_THAN)
                                            }
                                            if (requisitesProb == 0.0 || requisiteComparisonProb == 0.0) {
                                                subParameterValue = 0.0
                                            }
                                        }
                                    }
                                }

                                // remove "sub_" prefix for sub stat size retrieval
                                val parentStatement = statementParameterMap[parameter.name.removeRange(0..3)]!!
                                subStatProbMap[subStat] = subParameterValue / subStatSizeMap[parentStatement]!!

                                // disable while loops due to lack of implementation in Tint
                                if (tintSafe) {
                                    if (subParameter.name == "while_loop") {
                                        subStatProbMap[subStat] = 0.0
                                    }
                                }
                            }
                        }
                    }
                    else                                   -> throw Exception("Attempt to access SubStat data for unknown Stat $parameterValue!")
                }
            }
        }
        if (prob(AssignmentEqStat.ASSIGN_SIMPLE) == 0.0) {
            throw Exception("Simple assignment may not be completely disabled!")
        }
    }

    fun prob(given: Any): Double {
        return when (given) {
            is Double       -> given
            is WGSLType     -> typeProb(given)
            is Expr         -> exprProb(given)
            is Stat         -> statProb(given)
            is ArrayList<*> -> listProb(given)
            else            -> throw Exception("Attempt to retrieve probability for given of unknown type $given!")
        }
    }

    private fun typeProb(type: WGSLType): Double {
        val hashedProb = typeProbMap[type]
        if (hashedProb != null) {
            return hashedProb
        }

        when (type) {
            abstractWGSLScalarType -> {
                typeProbMap[type] = scalarProbTotal
                return scalarProbTotal
            }
            is WGSLVectorType      -> {
                var vecProb = prob(abstractWGSLVectorType)
                if (vecProb != 0.0) {
                    if (type.length != 0) vecProb /= 3
                    vecProb *= (prob(type.componentType) / scalarProbTotal)
                }
                typeProbMap[type] = vecProb
                return vecProb
            }
            is WGSLMatrixType      -> {
                var matProb = prob(abstractWGSLMatrixType)
                if (matProb != 0.0) {
                    if (type.width != 0) matProb /= 3
                    if (type.length != 0) matProb /= 3
                    matProb *= (prob(type.componentType) / matCompProbTotal)
                }
                typeProbMap[type] = matProb
                return matProb
            }
            is WGSLArrayType       -> {
                var arrProb = prob(abstractWGSLArrayType)
                if (arrProb != 0.0) {
                    if (type.elementCountValue != 0) arrProb /= maxArrayElementCount
                    arrProb *= (prob(type.elementType) / arrElemProbTotal)
                }
                typeProbMap[type] = arrProb
                return arrProb
            }
        }

        throw Exception("Attempt to retrieve probability of unknown type $type!")
    }

    private fun exprProb(expr: Expr): Double {
        val hashedProb = exprProbMap[expr]
        if (hashedProb != null) {
            return hashedProb
        }

        // disable certain exprs unsupported in Tint, naga or both

        if (tintSafe || nagaSafe) {
            if (expr == BuiltinIntegerExpr.SHIFT_LEFT || expr == BuiltinIntegerExpr.SHIFT_RIGHT || expr ==
                BuiltinFloatExpr.QUANTIZE_TO_F16) {
                exprProbMap[expr] = 0.0
                return 0.0
            }
        }
        if (nagaSafe) {
            if (expr == BuiltinFloatExpr.LDEXP || expr == BuiltinFloatVectorExpr.REFRACT || expr ==
                BuiltinIntegerExpr.COUNT_LEADING_ZEROS || expr == BuiltinIntegerExpr.COUNT_TRAILING_ZEROS) {
                exprProbMap[expr] = 0.0
                return 0.0
            }
        }

        val exprTypes = ExprTypes.exprTypeOf(expr).types
        if (exprTypes.size == 0 || prob(exprTypes) == 0.0) {
            exprProbMap[expr] = 0.0
            return 0.0
        }

        var probForAllExprTypes = 0.0
        for (exprType in exprTypes) {
            val concreteExprTypes = getConcreteTypes(exprType)

            for (concreteExprType in concreteExprTypes) {
                val probForExprType = when {
                    expr == BinaryArithmeticMatrixNumericExpr.MULT
                            && concreteExprType is WGSLMatrixType -> {
                        val (configValue, probConfigEnabled, probConfigDisabled) = getConfigValidity(
                            BinaryExpression, expr, concreteExprType, replaceMatrixBinaryOperandWithOtherTypeUserdef
                        )
                        replaceMatrixBinaryOperandWithOtherType = configValue

                        (replaceMatrixBinaryOperandWithOtherType * probConfigEnabled) +
                                ((1 - replaceMatrixBinaryOperandWithOtherType) * probConfigDisabled)
                    }
                    (expr is BinaryArithmeticMatrixNumericExpr || expr is BinaryArithmeticNumericExpr)
                            && concreteExprType is WGSLVectorType -> {
                        val (configValue, probConfigEnabled, probConfigDisabled) = getConfigValidity(
                            BinaryExpression, expr, concreteExprType, replaceVectorBinaryOperandWithOtherTypeUserdef
                        )
                        replaceVectorBinaryOperandWithOtherType = configValue

                        (replaceVectorBinaryOperandWithOtherType * probConfigEnabled) +
                                ((1 - replaceVectorBinaryOperandWithOtherType) * probConfigDisabled)
                    }

                    else                                          -> {
                        when (expr) {
                            is AccessExpr     -> prob(AccessExpression.argsForExprType(expr, concreteExprType))
                            is BinaryExpr     ->
                                prob(ArrayList(BinaryExpression.argsForExprType(expr, concreteExprType).unzip().first))
                            is BuiltinExpr    -> {
                                prob(BuiltinExpression.argsForExprType(expr, concreteExprType))
                            }
                            is ComparisonExpr -> prob(ComparisonExpression.argsForExprType(expr, concreteExprType))
                            is ConversionExpr -> prob(ConversionExpression.argsForExprType(expr, concreteExprType))
                            is DataExpr       -> prob(DataExpression.argsForExprType(expr, concreteExprType))
                            is IdentityExpr   -> prob(IdentityExpression.argsForExprType(expr, concreteExprType))
                            is UnaryExpr      -> prob(UnaryExpression.argsForExprType(expr, concreteExprType))
                            else              -> 0.0
                        }
                    }
                }
                probForAllExprTypes += (probForExprType * prob(concreteExprType))
            }
        }

        if (probForAllExprTypes == 0.0 || !probForAllExprTypes.isFinite()) {
            exprProbMap[expr] = 0.0
            return 0.0
        }

        if (expr == IdentityUniversalExpr.ZERO_VALUE) {
            probForAllExprTypes *= ratioSymbolSelectionToZeroValue
        }

        val parentExpression = when (expr) {
            is AccessExpr     -> AccessExpression
            is BinaryExpr     -> BinaryExpression
            is BuiltinExpr    -> BuiltinExpression
            is ComparisonExpr -> ComparisonExpression
            is ConversionExpr -> ConversionExpression
            is DataExpr       -> DataExpression
            is IdentityExpr   -> IdentityExpression
            is UnaryExpr      -> UnaryExpression
            else              -> throw Exception("Attempt to evaluate parent expression of unknown Expr $expr!")
        }

        val exprProb = expressionProbMap[parentExpression]!! * probForAllExprTypes
        exprProbMap[expr] = exprProb
        return exprProb
    }

    private fun statProb(stat: Stat): Double {
        val hashedProb = statProbMap[stat]
        if (hashedProb != null) {
            return hashedProb
        }

        val parentStatement = when (stat) {
            is AssignmentStat      -> AssignmentStatement
            is ContextSpecificStat -> ContextSpecificStatement
            is ControlFlowStat     -> ControlFlowStatement
            else                   -> throw Exception("Attempt to evaluate parent statement of unknown Stat $stat!")
        }

        val statUsedTypes = parentStatement.usedTypes(stat)
        if (statUsedTypes.isNotEmpty() && prob(statUsedTypes) == 0.0) {
            statProbMap[stat] = 0.0
            return 0.0
        }

        val subStatProb = subStatProbMap.getOrDefault(stat, 1.0 / subStatSizeMap[parentStatement]!!)

        val statProb = statementProbMap[parentStatement]!! * subStatProb
        statProbMap[stat] = statProb
        return statProb
    }

    private fun listProb(list: ArrayList<*>): Double {
        if (list.size == 0) {
            return 0.0
        }

        var listProb = 0.0
        for (item in list) {
            val prob = if (item is WGSLType) {
                prob(item)
            } else if (item is ArrayList<*> && item[0] is WGSLType) {
                var itemProb = 0.0
                for (itemItem in item) {
                    // if one argument type cannot be generated, the whole list of argTypes cannot be generated
                    val itemItemProb = prob(itemItem as WGSLType)
                    if (itemItemProb == 0.0) {
                        itemProb = 0.0
                        break
                    } else {
                        itemProb += itemItemProb
                    }
                }
                itemProb / item.size
            } else {
                throw Exception("Attempt to evaluate probability of list of unknown type $list!")
            }
            listProb += prob
        }

        return listProb / list.size
    }

    private fun getConfigValidity(
        expression: ExpressionCompanion, expr: Expr, concreteExprType: WGSLType, originalConfigValue: Double
    ): Triple<Double, Double, Double> {
        val probConfigEnabled = prob(
            ArrayList(
                if (expression == BinaryExpression) {
                    BinaryExpression.argsForExprType(expr, concreteExprType, true).unzip().first
                } else {
                    expression.argsForExprType(expr, concreteExprType, true)
                }
            )
        )
        val probConfigDisabled = prob(
            ArrayList(
                if (expression == BinaryExpression) {
                    BinaryExpression.argsForExprType(expr, concreteExprType, false).unzip().first
                } else {
                    expression.argsForExprType(expr, concreteExprType, false)
                }
            )
        )

        val configValue = if (probConfigEnabled == 0.0) {
            0.0 // set config as DISABLED i.e. 0.0
        } else if (probConfigDisabled == 0.0) {
            1.0 // set config as ENABLED i.e. 1.0
        } else {
            originalConfigValue
        }

        return Triple(configValue, probConfigEnabled, probConfigDisabled)
    }
}