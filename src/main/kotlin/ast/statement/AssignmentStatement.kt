package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.ast.expression.BinaryExpression.Companion.probEval
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.CNFG.nagaSafe
import wgslsmith.wgslgenerator.utils.PRNG

internal class AssignmentStatement(
    symbolTable: SymbolTable, override var stat: Stat,
    mustBeNewSymbol: Boolean = false, mustBeExistingSymbol: Boolean = false
) : Statement {
    private var lhs: Symbol
    private var rhs: Expression
    private var exprEquivalent: Expr? = null
    private var declaredNewSymbol = false
    private var accessString = ""

    init {
        val possibleTypes = ArrayList<WGSLType>()

        for (possibleType in usedTypes(stat)) {
            if (!mustBeExistingSymbol || symbolTable.hasWriteableOf(possibleType)) {
                possibleTypes.add(possibleType)
            }
        }

        val type = PRNG.getRandomTypeFrom(possibleTypes)

        if (stat == AssignmentCompoundStat.BINARY_OPERATOR) {
            val possibleExprEquivalents = ArrayList<Expr>()
            possibleExprEquivalents.addAll(compoundAssignableExprs)
            for (expr in compoundAssignableExprs) {
                if (type !in ExprTypes.exprTypeOf(expr).types.fold(ArrayList<WGSLType>()) { acc, accType ->
                        ArrayList(acc + getConcreteTypes(accType))
                    }) {
                    possibleExprEquivalents.remove(expr)
                }
            }
            exprEquivalent = PRNG.getRandomExprFrom(possibleExprEquivalents)
        }

        if (stat is AssignmentCompoundStat && !symbolTable.hasWriteableOf(type)) {
            stat = AssignmentEqStat.ASSIGN_SIMPLE
        }

        rhs = generateRHS(symbolTable, stat, type)

        when (stat) {
            is AssignmentCompoundStat      -> lhs = symbolTable.getRandomWriteableSymbol(type)
            AssignmentEqStat.ASSIGN_DECLARE,
            AssignmentEqStat.ASSIGN_LET    -> {
                lhs = symbolTable.declareNewNonWriteableSymbol(type)
                declaredNewSymbol = true
            }
            AssignmentEqStat.ASSIGN_PHONY  -> lhs = Symbol("_", type)
            AssignmentEqStat.ASSIGN_SIMPLE -> {
                if (mustBeNewSymbol || (!mustBeExistingSymbol &&
                            (PRNG.eval(CNFG.assignExpressionToNewVariable) || !symbolTable.hasWriteableOf(type)))) {
                    lhs = symbolTable.declareNewWriteableSymbol(type)
                    declaredNewSymbol = true
                } else {
                    lhs = symbolTable.getRandomWriteableSymbol(type)
                }
            }
            else                           -> throw Exception(
                "Attempt to generate AssignmentStat with unknown Stat ${stat}!"
            )
        }
    }

    private fun generateRHS(symbolTable: SymbolTable, stat: Stat, type: WGSLType): Expression {
        if (stat is AssignmentCompoundStat) {
            val lhsRhsTypes = BinaryExpression.argsForExprType(exprEquivalent!!, type, probEval(type))
                .filter { (lhsType, _) -> lhsType == type }
            val rhsType = PRNG.getRandomTypeFrom(lhsRhsTypes.unzip().second as ArrayList<WGSLType>)
            rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, 0)

            // handle lack of implementation in naga of mixed operands during compound assignment
            if (nagaSafe && rhs.returnType != type) {
                val rhsNonMixedType =
                    if (exprEquivalent == BinaryArithmeticMatrixNumericExpr.MULT && type is WGSLMatrixType) {
                        if (rhs.returnType is WGSLMatrixType) {
                            rhs.returnType
                        } else {
                            WGSLMatrixType(type.componentType, type.width, type.width)
                        }
                    } else {
                        type
                    }
                return IdentityZeroValExpression(rhsNonMixedType, IdentityUniversalExpr.ZERO_VALUE)
            }
            return rhs
        } else {
            return ExpressionGenerator.getExpressionWithReturnType(symbolTable, type, 0)
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (stat == AssignmentEqStat.ASSIGN_DECLARE) {
            return arrayListOf("var $lhs: ${lhs.type};")
        }

        val operator = if (stat == AssignmentCompoundStat.BINARY_OPERATOR) {
            "${exprEquivalent!!.operator}="
        } else {
            (stat as AssignmentStat).operator
        }
        if (stat == AssignmentCompoundStat.DECREMENT || stat == AssignmentCompoundStat.INCREMENT) {
            return arrayListOf("$lhs$operator;")
        }

        val varDeclaration = if (declaredNewSymbol) {
            if (stat == AssignmentEqStat.ASSIGN_LET) "let " else "var "
        } else ""

        val typeDeclaration = if (declaredNewSymbol && !PRNG.eval(CNFG.omitTypeFromDeclaration)) {
            ": ${lhs.type}"
        } else ""

        return arrayListOf("$varDeclaration$lhs$accessString$typeDeclaration $operator $rhs;")
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            if (stat is AssignmentCompoundStat) {
                return when (stat) {
                    AssignmentCompoundStat.DECREMENT,
                    AssignmentCompoundStat.INCREMENT       -> arrayListOf(scalarIntType, scalarUnIntType)
                    AssignmentCompoundStat.BINARY_OPERATOR -> compoundAssignableConcreteTypes
                }
            }

            return allConcreteTypes
        }
    }
}