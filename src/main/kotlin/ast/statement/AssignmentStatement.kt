package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.ast.scalarUnIntType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class AssignmentStatement : Statement {
    private lateinit var lhs: Symbol
    private lateinit var rhs: Expression
    private lateinit var type: WGSLType
    private var declaredNewSymbol = false
    private var compoundBinaryOperator = ""
    private var accessString = ""

    override lateinit var stat: Stat

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): AssignmentStatement {
        this.stat = stat

        if (stat is AssignCompoundStat) {
            val exprEquivalent = when (stat) {
                AssignCompoundStat.DECREMENT       -> BinaryArithmeticMatrixNumericExpr.MINUS
                AssignCompoundStat.INCREMENT       -> BinaryArithmeticMatrixNumericExpr.ADD
                AssignCompoundStat.BINARY_OPERATOR -> PRNG.getRandomExprFrom(compoundAssignableExprs)
            }
            type = when (stat) {
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

            val binaryExpressionEquivalent = BinaryExpression().generate(
                symbolTable, type, exprEquivalent, 0
            )

            rhs = binaryExpressionEquivalent.getRHS()

            // temporary code to handle lack of implementation in naga of mixed operands during compound assignment
            if (rhs.returnType != type) {
                rhs = IdentityZeroValExpression().generate(symbolTable, type, IdentityUniversalExpr.ZERO_VALUE, 0)
            }
        } else {
            rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
            type = rhs.returnType
        }

        // check if the selected compound statement can be used, and revert to simple assignment if not
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
                if ((PRNG.evaluateProbability(CNFG.probabilityAssignToNewSymbol) && this.stat is AssignEqStat)
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

        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (stat == AssignEqStat.ASSIGN_DECLARE) {
            return arrayListOf("var $lhs;")
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
            PRNG.evaluateProbability(CNFG.probabilityOmitTypeFromDeclaration)
        ) {
            ": $type"
        } else ""

        return arrayListOf("$varDeclaration$lhs$accessString$typeDeclaration $operator $rhs;")
    }
}