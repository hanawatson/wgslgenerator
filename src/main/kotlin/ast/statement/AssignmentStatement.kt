package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.*
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.ast.expression.BinaryExpression.Companion.probEval
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class AssignmentStatement(symbolTable: SymbolTable, override var stat: Stat) : Statement {
    private var lhs: Symbol
    private var rhs: Expression
    private var type: WGSLType
    private var exprEquivalent: Expr? = null
    private var declaredNewSymbol = false
    private var accessString = ""

    init {
        type = if (stat == AssignmentCompoundStat.BINARY_OPERATOR) {
            exprEquivalent = PRNG.getRandomExprFrom(compoundAssignableExprs)
            PRNG.getRandomTypeFrom(ExprTypes.exprTypeOf(exprEquivalent!!).types)
        } else {
            when (stat) {
                AssignmentCompoundStat.DECREMENT -> exprEquivalent = BinaryArithmeticMatrixNumericExpr.MINUS
                AssignmentCompoundStat.INCREMENT -> exprEquivalent = BinaryArithmeticMatrixNumericExpr.ADD
            }
            PRNG.getRandomTypeFrom(usedTypes(stat))
        }

        if (stat is AssignmentCompoundStat) {
            val lhsRhsTypes = BinaryExpression.argsForExprType(exprEquivalent!!, type, probEval(exprEquivalent!!, type))
                .filter { (lhsType, _) -> lhsType == type }
            val rhsType = PRNG.getRandomTypeFrom(lhsRhsTypes.unzip().second as ArrayList<WGSLType>)
            rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, 0)

            // temporary code to handle lack of implementation in naga of mixed operands during compound assignment
            if (rhs.returnType != type) {
                val rhsNonMixedType =
                    if (exprEquivalent == BinaryArithmeticMatrixNumericExpr.MULT && type is WGSLMatrixType) {
                        if (rhs.returnType is WGSLMatrixType) {
                            rhs.returnType
                        } else {
                            WGSLMatrixType(
                                (type as WGSLMatrixType).componentType,
                                (type as WGSLMatrixType).width,
                                (type as WGSLMatrixType).width
                            )
                        }
                    } else {
                        type
                    }
                rhs = IdentityZeroValExpression(rhsNonMixedType, IdentityUniversalExpr.ZERO_VALUE)
            }
        } else {
            rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
            type = rhs.returnType
        }
        if (stat is AssignmentCompoundStat && !symbolTable.hasWriteableOf(type)) {
            this.stat = AssignmentEqStat.ASSIGN_SIMPLE
            type = rhs.returnType
        }
        when (this.stat) {
            AssignmentEqStat.ASSIGN_DECLARE,
            AssignmentEqStat.ASSIGN_LET   -> {
                lhs = symbolTable.declareNewNonWriteableSymbol(type)
                declaredNewSymbol = true
            }
            AssignmentEqStat.ASSIGN_PHONY -> lhs = Symbol("_", type)
            AssignmentEqStat.ASSIGN_SIMPLE,
            is AssignmentCompoundStat     -> {
                if ((PRNG.eval(CNFG.assignExpressionToNewVariable) && this.stat is AssignmentEqStat)
                    || (!symbolTable.hasWriteableOf(type) && this.stat !is AssignmentCompoundStat)) {
                    lhs = symbolTable.declareNewWriteableSymbol(type)
                    declaredNewSymbol = true
                } else {
                    lhs = symbolTable.getRandomWriteableSymbol(type)
                }
            }
            else                          -> throw Exception(
                "Attempt to generate AssignmentStat with unknown Stat ${this.stat}!"
            )
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (stat == AssignmentEqStat.ASSIGN_DECLARE) {
            return arrayListOf("var $lhs: $type;")
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
        val typeDeclaration = if (declaredNewSymbol &&
            PRNG.eval(CNFG.omitTypeFromDeclaration)
        ) {
            ": $type"
        } else ""

        return arrayListOf("$varDeclaration$lhs$accessString$typeDeclaration $operator $rhs;")
    }

    companion object : StatementCompanion {
        override fun usedTypes(stat: Stat): ArrayList<WGSLType> {
            if (stat is AssignmentCompoundStat) {
                return when (stat) {
                    AssignmentCompoundStat.DECREMENT,
                    AssignmentCompoundStat.INCREMENT       -> arrayListOf(scalarIntType, scalarUnIntType)
                    AssignmentCompoundStat.BINARY_OPERATOR -> compoundAssignableExprs.fold(ArrayList()) { acc, expr ->
                        ArrayList(acc + ExprTypes.exprTypeOf(expr).types)
                    }
                }
            }

            return allTypes
        }
    }
}