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
    private var declaredNewSymbol = false
    private var compoundBinaryOperator = ""
    private var accessString = ""

    init {
        if (stat is AssignCompoundStat) {
            val exprEquivalent = when (stat as AssignCompoundStat) {
                AssignCompoundStat.DECREMENT       -> BinaryArithmeticMatrixNumericExpr.MINUS
                AssignCompoundStat.INCREMENT       -> BinaryArithmeticMatrixNumericExpr.ADD
                AssignCompoundStat.BINARY_OPERATOR -> PRNG.getRandomExprFrom(compoundAssignableExprs)
            }
            type = when (stat as AssignCompoundStat) {
                AssignCompoundStat.DECREMENT,
                AssignCompoundStat.INCREMENT       -> PRNG.getRandomTypeFrom(
                    arrayListOf(scalarIntType, scalarUnIntType)
                )
                AssignCompoundStat.BINARY_OPERATOR -> {
                    compoundBinaryOperator = "${exprEquivalent.operator}="
                    val exprType = ExprTypes.typeOf(exprEquivalent)
                    PRNG.getRandomTypeFrom(exprType.types)
                }
            }

            val lhsRhsTypes = BinaryExpression.argsForExprType(exprEquivalent, type, probEval(exprEquivalent, type))
                .filter { (lhsType, _) -> lhsType == type }
            val rhsType = PRNG.getRandomTypeFrom(lhsRhsTypes.unzip().second as ArrayList<WGSLType>)
            rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, 0)

            // temporary code to handle lack of implementation in naga of mixed operands during compound assignment
            if (!(type is WGSLMatrixType && rhs.returnType is WGSLMatrixType) && rhs.returnType != type) {
                rhs = IdentityZeroValExpression(type, IdentityUniversalExpr.ZERO_VALUE)
            }
        } else {
            rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
            type = rhs.returnType
        }
        if (stat is AssignCompoundStat && !symbolTable.hasWriteableOf(type)) {
            this.stat = AssignEqStat.ASSIGN_SIMPLE
            type = rhs.returnType
        }
        when (this.stat) {
            AssignEqStat.ASSIGN_DECLARE,
            AssignEqStat.ASSIGN_LET -> {
                lhs = symbolTable.declareNewNonWriteableSymbol(type)
                declaredNewSymbol = true
            }
            // AssignEqStat.ASSIGN_PHONY -> lhs = Symbol("_", type)
            AssignEqStat.ASSIGN_SIMPLE,
            is AssignCompoundStat   -> {
                if ((PRNG.eval(CNFG.assignExpressionToNewVariable) && this.stat is AssignEqStat)
                    || (!symbolTable.hasWriteableOf(type) && this.stat !is AssignCompoundStat)) {
                    lhs = symbolTable.declareNewWriteableSymbol(type)
                    declaredNewSymbol = true
                } else {
                    lhs = symbolTable.getRandomWriteableSymbol(type)
                }
            }
            else                    -> throw Exception(
                "Attempt to generate AssignmentStat with unknown Stat ${this.stat}!"
            )
        }
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (stat == AssignEqStat.ASSIGN_DECLARE) {
            return arrayListOf("var $lhs: $type;")
        }

        val operator = if (stat == AssignCompoundStat.BINARY_OPERATOR) {
            compoundBinaryOperator
        } else {
            (stat as AssignStat).operator
        }
        if (stat == AssignCompoundStat.DECREMENT || stat == AssignCompoundStat.INCREMENT) {
            return arrayListOf("$lhs$operator;")
        }

        val varDeclaration = if (declaredNewSymbol) {
            if (stat == AssignEqStat.ASSIGN_LET) "let " else "var "
        } else ""
        val typeDeclaration = if (declaredNewSymbol &&
            PRNG.eval(CNFG.omitTypeFromDeclaration)
        ) {
            ": $type"
        } else ""

        return arrayListOf("$varDeclaration$lhs$accessString$typeDeclaration $operator $rhs;")
    }
}